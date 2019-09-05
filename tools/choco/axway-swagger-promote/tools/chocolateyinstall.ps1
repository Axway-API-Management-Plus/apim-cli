$ErrorActionPreference = 'Stop'; # stop on all errors
$toolsDir   = "$(Split-Path -parent $MyInvocation.MyCommand.Definition)"
$fileLocation = Join-Path $toolsDir 'apimanager-swagger-promote-1.6.2.zip'

$packageArgs = @{
  packageName   = $env:ChocolateyPackageName
  unzipLocation = $toolsDir
  file          = $fileLocation

  softwareName  = 'axway-swagger-promote'
  checksum      = '668E387EC21D9C355D30EC02999387D85F8DBD4EE56E6844FCA70097590CD060EB0CECB124909F8E6905D838066398D992B8B822F33E89EAB75BB240A3741BF0'
  checksumType  = 'sha256'
}

Get-ChocolateyUnzip "$fileLocation" $toolsDir
Install-BinFile -Name "api-import" -Path "$toolsDir\swagger-promote-1.6.2-SNAPSHOT\scripts\api-import.bat" -Command "choco"
Install-BinFile -Name "api-export" -Path "$toolsDir\swagger-promote-1.6.2-SNAPSHOT\scripts\api-export.bat" -Command "choco"

Write-Output "------------------------------------------------------------------------"
Write-Output "Axway Swagger-Promote has been installed."
Write-Output ""
Write-Output "Execute: 'api-import' to import new APIs"
Write-Output "Execute: 'api-export' to export existing APIs"
Write-Output "------------------------------------------------------------------------"
