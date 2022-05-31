param(
  [Parameter(Mandatory = $false, HelpMessage = "The base branch that this pull request is merged into")] 
  [string]$BaseBranch = 'main',
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
        Throw "Repository: $($repository.path) commit changes before creating a PR or use -Force"
      } 
    }
  }
 
  foreach ($repository in $scmConfig.repositories) {
    Write-Output $repository.path
    Push-Location -Path $repository.path 
    try {
      git push --set-upstream origin HEAD
      gh pr create -f --base $BaseBranch
    }
    finally {
      Pop-Location
    }
  }
}
finally {
  Pop-Location
}
