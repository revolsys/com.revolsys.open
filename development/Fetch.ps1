param(
)

$scmConfig = Get-Content "$PSScriptRoot/Config.json" | ConvertFrom-Json

try {
  Push-Location -Path "$PSScriptRoot/../.."
 
  foreach ($repository in $scmConfig.repositories) {
    Write-Output $repository.path
    Write-Output "-  origin"
    git -C $repository.path fetch 'origin'
    foreach ($remote in $repository.remotes.psobject.properties.name) {
      Write-Output "-  $remote"
      git -C $repository.path fetch $remote
    }
    Write-Output "-  upstream tags"
    git -C $repository.path fetch 'upstream' --tags  
  }
}
finally {
  Pop-Location
}