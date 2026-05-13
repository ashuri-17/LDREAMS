$repoPath = "C:\Users\Administrator\Documents\downlooods\LDreams"
Set-Location $repoPath
$null = & "git" @("init")
$null = & "git" @("checkout", "-b", "main")
$null = & "git" @("add", "-A")
$null = & "git" @("commit", "-m", "Initial commit: LDREAMS lucid dreaming Android app")
Write-Host "Git repo initialized and files committed!"
