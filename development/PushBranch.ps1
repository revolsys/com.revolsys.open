
param(
  [Parameter(Mandatory = $false, HelpMessage = "Force pushing even if there an uncommitted changes")] 
  [Switch]$Force = $false
)

$scmConfig = Get-Content "$PSScriptRoot/Config.json" | ConvertFrom-Json

try {
  Push-Location -Path "$PSScriptRoot/../.."

  if ( !($Force.IsPresent)) {
    foreach ($repository in $scmConfig.repositories) {
      if ( git -C $repository.path status --porcelain ) {
        Write-Output $repository.path
        Throw "Repository: $($repository.path) commit changes before pushing or use -Force"
      } 
    }
  }
 
  foreach ($repository in $scmConfig.repositories) {
    Write-Output $repository.path
    git -C $repository.path push --set-upstream origin HEAD
  }
}
finally {
  Pop-Location
}