$htmlDir = "src\main\resources\templates\html"
$files = Get-ChildItem -Path $htmlDir -Filter "*.html" -Recurse
$count = 0
foreach ($file in $files) {
    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
    $content = [System.Text.Encoding]::UTF8.GetString($bytes)
    # Replace the double-encoded corruption: â‚± -> ₱
    $badSeq = [System.Text.Encoding]::UTF8.GetString([byte[]](0xC3, 0xA2, 0xE2, 0x80, 0x9A, 0xC2, 0xB1))
    $good = [System.Text.Encoding]::UTF8.GetString([byte[]](0xE2, 0x82, 0xB1))
    $fixed = $content.Replace($badSeq, $good)
    if ($fixed -cne $content) {
        $utf8NoBom = New-Object System.Text.UTF8Encoding $false
        [System.IO.File]::WriteAllText($file.FullName, $fixed, $utf8NoBom)
        Write-Output ("Fixed: " + $file.Name)
        $count++
    }
}
Write-Output ("Total fixed: " + $count)
