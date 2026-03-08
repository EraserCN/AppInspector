$gradleCache = "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1"
$localMaven = "$PSScriptRoot\local-maven"

function Copy-Artifact {
    param($group, $artifact, $version)
    $srcDir = Join-Path $gradleCache "$group\$artifact\$version"
    if (-not (Test-Path $srcDir)) { Write-Host "MISSING: $group:$artifact:$version"; return }
    
    $groupPath = $group -replace '\.', '\'
    $destDir = Join-Path $localMaven "$groupPath\$artifact\$version"
    New-Item -ItemType Directory -Force -Path $destDir | Out-Null
    
    Get-ChildItem $srcDir -Recurse -File | ForEach-Object {
        Copy-Item $_.FullName -Destination "$destDir\$($_.Name)" -Force
    }
    Write-Host "Copied: $group:$artifact:$version"
}

# AGP 8.2.0
Copy-Artifact "com.android.tools.build" "gradle" "8.2.0"
Copy-Artifact "com.android.tools.build" "gradle-api" "8.2.0"
Copy-Artifact "com.android.tools.build" "gradle-settings-api" "8.2.0"

# Kotlin 1.9.0
Copy-Artifact "org.jetbrains.kotlin" "kotlin-gradle-plugin" "1.9.0"
Copy-Artifact "org.jetbrains.kotlin" "kotlin-gradle-plugin-api" "1.9.0"
Copy-Artifact "org.jetbrains.kotlin" "kotlin-gradle-plugin-annotations" "1.9.0"
Copy-Artifact "org.jetbrains.kotlin" "kotlin-gradle-plugins-bom" "1.9.0"

Write-Host "Done! Local Maven repo at: $localMaven"
