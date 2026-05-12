$dirs = @(
    "src\main\resources\templates\html",
    "src\main\resources\static\js"
)

$count = 0
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

foreach ($dir in $dirs) {
    $files = Get-ChildItem -Path $dir -Filter "*.*" -Recurse
    foreach ($file in $files) {
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
        $content = [System.Text.Encoding]::UTF8.GetString($bytes)
        $fixed = $content

        # Fix em dash — (E2 80 94) corrupted as â€" (C3 A2 E2 80 9C 22)
        $fixed = $fixed.Replace(
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xC3,0xA2,0xE2,0x80,0x9C,0x22)),
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xE2,0x80,0x94))
        )
        # Fix en dash – (E2 80 93) corrupted as â€" (same bytes, different context)
        # Fix non-breaking hyphen â€' (E2 80 91)
        $fixed = $fixed.Replace(
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xC3,0xA2,0xE2,0x80,0x9C,0x91)),
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xE2,0x80,0x91))
        )
        # Fix ellipsis … (E2 80 A6) corrupted as â€¦ (C3 A2 E2 80 9C A6)
        $fixed = $fixed.Replace(
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xC3,0xA2,0xE2,0x80,0x9C,0xA6)),
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xE2,0x80,0xA6))
        )
        # Fix left double quote " (E2 80 9C) corrupted as â€œ (C3 A2 E2 80 9C 9C)
        $fixed = $fixed.Replace(
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xC3,0xA2,0xE2,0x80,0x9C,0x9C)),
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xE2,0x80,0x9C))
        )
        # Fix right double quote " (E2 80 9D) corrupted as â€ (C3 A2 E2 80 9C 9D)
        $fixed = $fixed.Replace(
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xC3,0xA2,0xE2,0x80,0x9C,0x9D)),
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xE2,0x80,0x9D))
        )
        # Fix right arrow → (E2 86 92) corrupted as â†' (C3 A2 E2 86 92)
        $fixed = $fixed.Replace(
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xC3,0xA2,0xE2,0x86,0x92)),
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xE2,0x86,0x92))
        )
        # Fix middle dot · (C2 B7) corrupted as Â· (C3,0x82,0xC2,0xB7)
        $fixed = $fixed.Replace(
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xC3,0x82,0xC2,0xB7)),
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xC2,0xB7))
        )
        # Fix times × (C3 97) corrupted as Ã— (C3,0x83,0xC3,0x97)
        $fixed = $fixed.Replace(
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xC3,0x83,0xC3,0x97)),
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xC3,0x97))
        )
        # Fix ₱ (E2 82 B1) corrupted as â‚± (C3 A2 E2 82 B1)
        $fixed = $fixed.Replace(
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xC3,0xA2,0xE2,0x82,0xB1)),
            [System.Text.Encoding]::UTF8.GetString([byte[]](0xE2,0x82,0xB1))
        )

        if ($fixed -cne $content) {
            [System.IO.File]::WriteAllText($file.FullName, $fixed, $utf8NoBom)
            Write-Output ("Fixed: " + $file.Name)
            $count++
        }
    }
}
Write-Output ("Total files fixed: " + $count)
