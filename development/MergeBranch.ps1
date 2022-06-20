param(
  [Parameter(Mandatory = $false, HelpMessage = "The branch name to merge (e.g. feature/new-ui)")] 
  [string]$BranchName,
  [Parameter(Mandatory = $false, HelpMessage = "The name of the remote to fetch and merge the branch from (e.g. upstream)")] 
  [string]$RemoteName
)

$scmConfig = Get-Content "$PSScriptRoot/Config.json" | ConvertFrom-Json

try {
  Push-Location -Path "$PSScriptRoot/../.."
 
  foreach ($repository in $scmConfig.repositories) {
    Write-Output $repository.path
    if ($BranchName) {
      if ($RemoteName) {
        git -C $repository.path fetch $RemoteName
        git -C $repository.path merge "$RemoteName/$BranchName"
      }
      else {
        git -C $repository.path merge $BranchName
      }
    }
    else {
      if ( !($RemoteName)) {
        $RemoteName = 'upstream'
      }
      git -C $repository.path fetch $RemoteName
      git -C $repository.path merge "$RemoteName/main"
    }
    
  }
}
finally {
  Pop-Location
}