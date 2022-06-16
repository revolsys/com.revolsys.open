param(
  [Parameter(Mandatory = $true, HelpMessage = "The branch name to delete (e.g. feature/new-ui)")] 
  [string]$BranchName
)

$scmConfig = Get-Content "$PSScriptRoot/Config.json" | ConvertFrom-Json

try {
  Push-Location -Path "$PSScriptRoot/../.."

  foreach ($repository in $scmConfig.repositories) {
    $currentBranch = $(git -C $repository.path branch --show-current)
    if ($scmConfig.protectedBranches -contains $BranchName ) {
      Throw "Repository: $($repository.path) cannot delete protected branch $BranchName in $($scmConfig.protectedBranches)"
    } 
    if ($currentBranch -eq $BranchName) {
      Throw "Repository: $($repository.path) cannot delete current branch ${BranchName}"
    } 
  }
 
  foreach ($repository in $scmConfig.repositories) {
    Write-Output $repository.path
    git -C $repository.path branch -D $BranchName
    foreach ($remote in $repository.remotes.psobject.properties.name) {
      git -C $repository.path push $remote :$BranchName
    }
  }
}
finally {
  Pop-Location
}