FROM eclipse-temurin:17-jre-alpine  AS base
ENV APP_HOME=/opt/apim-cli
ENV APP_USER=axway
ARG APIM_CLI_ARCHIVE
ENV APIM_CLI_ARCHIVE=${APIM_CLI_ARCHIVE:-axway-apimcli-1.12.1.tar.gz}
COPY $APIM_CLI_ARCHIVE /
RUN "apk add --no-cache tar" \
    && "addgroup $APP_USER"  \
    && "adduser --system $APP_USER --ingroup $APP_USER" \
    && "mkdir $APP_HOME"  \
    && "tar -xvzf $APIM_CLI_ARCHIVE -C $APP_HOME --strip-components 1" \
    && "rm /$APIM_CLI_ARCHIVE"
FROM eclipse-temurin:11-jre-alpine
USER $APP_USER
ENV AXWAY_APIM_CLI_HOME $APP_HOME
COPY --from=base $APP_HOME $APP_HOME
COPY --from=base /etc/passwd /etc/passwd
COPY --from=base /etc/group /etc/group
WORKDIR /opt/apim-cli
ENTRYPOINT ["java", "-cp", "lib/*", "com.axway.apim.cli.APIManagerCLI"]
CMD ["arg1", "arg2"]

