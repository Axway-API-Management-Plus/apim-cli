$ErrorActionPreference = 'Stop'; # stop on all errors
$toolsDir   = "$(Split-Path -parent $MyInvocation.MyCommand.Definition)"
$fileLocation = Join-Path $toolsDir 'axway-apimcli-1.3.8.zip'

Get-ChocolateyUnzip "$fileLocation" $toolsDir
Install-BinFile -Name "apim" -Path "$toolsDir\apim-cli-$env:chocolateyPackageVersion\scripts\apim.bat" -Command "choco"

Write-Output "---------------------------------------------------------------------------"
Write-Output "Axway API-Management CLI: $env:chocolateyPackageVersion has been installed."
Write-Output ""
Write-Output "You may run different options:"
Write-Output "Execute: 'apim api import' to import new APIs"
Write-Output "Execute: 'apim api get -s qa' to list existing APIs on your QA stage"
Write-Output "Execute: 'apim api get -s dev -f json' to export existing APIs into JSON"
Write-Output "Execute: 'apim app import' to import applications"
Write-Output "Execute: 'apim app get -s prod' to list existing applications"
Write-Output "Execute: 'apim org get -s dev' to list existing organizations on Dev stage"
Write-Output "Execute: 'apim user get -s qa' to list existing users on QA stage"
Write-Output ""
Write-Output "Learn more: https://github.com/Axway-API-Management-Plus/apim-cli/wiki"
Write-Output "---------------------------------------------------------------------------"
