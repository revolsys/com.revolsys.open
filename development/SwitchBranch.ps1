param(
  [Parameter(Mandatory = $true, HelpMessage = "The branch name to switch to (e.g. feature/new-ui)")] 
  [string]$BranchName,
  [Parameter(Mandatory = $false, HelpMessage = "Force switching even if there an uncommitted changes")] 
  [Switch]$Force = $false
)

$scmConfig = Get-Content "$PSScriptRoot/Config.json" | ConvertFrom-Json

try {
  Push-Location -Path "$PSScriptRoot/../.."

  if ( !($Force.IsPresent)) {
    foreach ($repository in $scmConfig.repositories) {
      if ( git -C $repository.path status --porcelain ) {
        Write-Output $repository.path
        Throw "Repository: $($repository.path) commit changes before switching or use -Force"
      } 
    }
  }
 
  foreach ($repository in $scmConfig.repositories) {
    Write-Output $repository.path
    git -C $repository.path checkout $BranchName
  }
}
finally {
  Pop-Location
}