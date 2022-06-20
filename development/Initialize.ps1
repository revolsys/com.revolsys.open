$ErrorActionPreference = "Stop"
$scmConfig = Get-Content "$PSScriptRoot/Config.json" | ConvertFrom-Json

Push-Location -Path "$PSScriptRoot/../.."
try {
  foreach ($repository in $scmConfig.repositories) {
    Write-Output $repository.path

    # Fork and clone the repository
    if ( Test-Path $repository.path ) {
      gh repo fork $repository.remotes.upstream --clone=false 
    }
    else {
      gh repo fork $repository.remotes.upstream --clone=true --remote=true $repository.path
    }

    # Add all the remotes defined in the config
    foreach ($remote in $repository.remotes.psobject.properties.name) {
      git -C $repository.path remote remove $remote
      git -C $repository.path remote add -f $remote "git@github.com:$($repository.remotes.$remote)"
    }
  }
}
finally {
  Pop-Location
}
