$ErrorActionPreference = 'Stop'; # stop on all errors
$toolsDir   = "$(Split-Path -parent $MyInvocation.MyCommand.Definition)"
$fileLocation = Join-Path $toolsDir 'apimanager-swagger-promote-1.7.0-SNAPSHOT.zip'

Get-ChocolateyUnzip "$fileLocation" $toolsDir
Install-BinFile -Name "apim" -Path "$toolsDir\swagger-promote-$env:chocolateyPackageVersion\scripts\apim.bat" -Command "choco"

Write-Output "------------------------------------------------------------------------"
Write-Output "Axway Swagger-Promote: $env:chocolateyPackageVersion has been installed."
Write-Output ""
Write-Output "You may run different options:"
Write-Output "Execute: 'apim api import' to import new APIs"
Write-Output "Execute: 'apim api export' to export existing APIs"
Write-Output "Execute: 'apim app export' to export existing APIs"
Write-Output "------------------------------------------------------------------------"
