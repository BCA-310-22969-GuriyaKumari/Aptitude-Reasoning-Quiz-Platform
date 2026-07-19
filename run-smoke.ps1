# Smoke test script for Spring Boot quiz app
# Run from project root. Requires curl.exe and app running at http://localhost:9093

# Clean previous outputs
Remove-Item -Force -ErrorAction SilentlyContinue index.html admin_home.html after_add.html admin_questions.html after_delete.html admin_dashboard.html student_entry_after.html quiz_redirect.html quiz_page.html submit_body.txt result_page.html scoreboard.html admin_cookies.txt student_cookies.txt

# 1) Fetch home
curl.exe -s -c admin_cookies.txt http://localhost:9093/ -o index.html
Write-Output 'Saved index.html'

# 2) Admin login
curl.exe -s -b admin_cookies.txt -c admin_cookies.txt -d "username=admin&password=admin123" -X POST http://localhost:9093/admin/login -L -o admin_home.html
Write-Output 'Saved admin_home.html'

# 3) Add question (APTITUDE)
curl.exe -s -b admin_cookies.txt -c admin_cookies.txt -d "title=Smoke+Test+Q&optionA=Alpha&optionB=Beta&optionC=Gamma&correctAns=1&category=APTITUDE" -X POST http://localhost:9093/admin/questions/add -L -o after_add.html
Write-Output 'Saved after_add.html'

# 4) List questions
curl.exe -s -b admin_cookies.txt http://localhost:9093/admin/questions -o admin_questions.html
Write-Output 'Saved admin_questions.html'

# 5) Try to extract the newly added question id and delete it (PowerShell parsing)
$cont = Get-Content admin_questions.html -Raw
if ($cont -match 'name="id".*?value="(\d+)"') { $id=$matches[1]; Write-Output "Found question id: $id" } else { Write-Output "No id found" }
if ($id) { 
  curl.exe -s -b admin_cookies.txt -c admin_cookies.txt -d "id=$id" -X POST http://localhost:9093/admin/questions/delete -L -o after_delete.html
  Write-Output 'Saved after_delete.html'
}

# 6) Admin dashboard
curl.exe -s -b admin_cookies.txt http://localhost:9093/admin/dashboard -o admin_dashboard.html
Write-Output 'Saved admin_dashboard.html'

# 7) Student login
curl.exe -s -c student_cookies.txt -d "studentName=Tester" -X POST http://localhost:9093/student/entry -L -o student_entry_after.html
Write-Output 'Saved student_entry_after.html'

# 8) Student start quiz (APTITUDE)
curl.exe -s -b student_cookies.txt -c student_cookies.txt -d "subject=APTITUDE" -X POST http://localhost:9093/student/start -L -o quiz_redirect.html
Write-Output 'Saved quiz_redirect.html'

# 9) Get quiz page
curl.exe -s -b student_cookies.txt http://localhost:9093/quiz -o quiz_page.html
Write-Output 'Saved quiz_page.html'

# 10) Build submission using correctAns values and submit
$qp = Get-Content quiz_page.html -Raw
$rx = [regex] 'name="questions\[(\d+)\]\.correctAns"\s*value="(\d+)"'
$bodyParts = @()
foreach ($m in $rx.Matches($qp)) {
  $i = $m.Groups[1].Value
  $correct = $m.Groups[2].Value
  $bodyParts += "questions[$i].selectedAns=$correct"
  if ($qp -match "name=\"questions\[$i\]\.quesId\".*?value=\"(\d+)\"") { $qid = $matches[1]; $bodyParts += "questions[$i].quesId=$qid" }
  $bodyParts += "questions[$i].correctAns=$correct"
}
$bodyParts += "username=Tester"
$body = $bodyParts -join '&'
Set-Content -Path submit_body.txt -Value $body
Write-Output 'Saved submit_body.txt'

# Submit
curl.exe -s -b student_cookies.txt -c student_cookies.txt -H "Content-Type: application/x-www-form-urlencoded" --data-binary @submit_body.txt -X POST http://localhost:9093/submit -L -o result_page.html
Write-Output 'Saved result_page.html'

# 11) Scoreboard
curl.exe -s -b admin_cookies.txt http://localhost:9093/scoreboard -o scoreboard.html
Write-Output 'Saved scoreboard.html'

Write-Output 'Smoke test finished — open the generated files in VS Code: index.html, admin_home.html, admin_questions.html, admin_dashboard.html, quiz_page.html, result_page.html, scoreboard.html'
