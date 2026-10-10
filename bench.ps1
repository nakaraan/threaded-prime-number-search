# Times every variant at several thread counts and saves the results to results.csv
# Run from this folder:  powershell -ExecutionPolicy Bypass -File bench.ps1
#
# Output is sent to NUL so we measure the computation, not how fast the console can draw text.
# B1 variants use a bigger y than B2 variants because B2 is much slower (one sync point per number).

$runs    = 3
$threads = 1, 2, 4, 8, 16, 64
$variants = @(
    @{ name = "variant1-a1-b1"; y = 10000000 },
    @{ name = "variant2-a2-b1"; y = 10000000 },
    @{ name = "variant3-a1-b2"; y = 100000 },
    @{ name = "variant4-a2-b2"; y = 100000 }
)

$cpu = Get-CimInstance Win32_Processor | Select-Object -First 1
Write-Host "CPU  : $($cpu.Name)"
Write-Host "Cores: $($cpu.NumberOfCores) physical, $($cpu.NumberOfLogicalProcessors) logical"
Write-Host "Java : $(cmd /c 'java -version 2>&1' | Select-Object -First 1)"

$cfg = Join-Path $env:TEMP "bench-config.txt"
$results = @()

foreach ($v in $variants) {
    Push-Location $v.name
    javac --release 17 -d out src\primesearch\*.java
    if ($LASTEXITCODE -ne 0) { Pop-Location; throw "Build failed in $($v.name)" }

    foreach ($x in $threads) {
        "x = $x`ny = $($v.y)" | Set-Content $cfg
        for ($r = 1; $r -le $runs; $r++) {
            $t = Measure-Command { cmd /c "java -cp out primesearch.PrimeSearch `"$cfg`" > NUL" }
            $sec = [math]::Round($t.TotalSeconds, 3)
            Write-Host "$($v.name)  x=$x  y=$($v.y)  run $r : $sec s"
            $results += [pscustomobject]@{ variant = $v.name; x = $x; y = $v.y; run = $r; seconds = $sec }
        }
    }
    Pop-Location
}

$results | Export-Csv results.csv -NoTypeInformation
Write-Host "Saved results.csv"
