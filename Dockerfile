FROM eclipse-temurin:8u332-b09-jre-alpine  as base
COPY axway-apimcli-1.12.1.tar.gz /
RUN apk add --no-cache tar
RUN tar -xvzf axway-apimcli-1.12.1.tar.gz -C /opt && rm /axway-apimcli-1.12.1.tar.gz
FROM eclipse-temurin:8u332-b09-jre-alpine
COPY --from=base /opt/apim-cli-1.12.1 /opt/apim-cli-1.12.1
ENTRYPOINT ["java", "-cp", "/opt/apim-cli-1.12.1/lib/*", "com.axway.apim.cli.APIManagerCLI"]
CMD ["arg1", "arg2"]