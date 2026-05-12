$lines = [System.IO.File]::ReadAllLines("C:\Users\Charlene C. Dilig\OneDrive\Documents\Github\payroll\src\main\resources\templates\html\addEmployee.html", [System.Text.Encoding]::UTF8)
for ($i = 121; $i -le 130; $i++) {
    Write-Host "Line $($i): $($lines[$i])"
    $chars = $lines[$i].ToCharArray()
    $hex = ($chars | ForEach-Object { [String]::Format("{0:X4}", [int]$_) }) -join " "
    Write-Host "  Hex: $hex"
}
