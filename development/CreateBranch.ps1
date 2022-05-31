param(
  [Parameter(Mandatory = $true, HelpMessage = "The branch name (e.g. feature/new-ui)")] 
  [string]$BranchName,
  [Parameter(Mandatory = $false, HelpMessage = "The remote to create the branch from")] 
  [string]$RemoteName

)
$ErrorActionPreference = "Stop"
$scmConfig = Get-Content "$PSScriptRoot/Config.json" | ConvertFrom-Json

Push-Location -Path "$PSScriptRoot/../.."
try {
  foreach ($repository in $scmConfig.repositories) {
    Write-Output $repository.path
    $gitDir = $repository.path
    if ($null -eq $RemoteName) {
      git -C $gitDir checkout -b $BranchName 
    }
    else {
      git -C $gitDir fetch $RemoteName
      git -C $gitDir checkout -b $BranchName "$RemoteName/$BranchName"
    }
  }
}
finally {
  Pop-Location
}
 