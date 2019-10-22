## Plugins for Swagger-Promote

Swagger-Promote is already very feature rich and provides a lot of flexibility. However, some of the enhancement requests
shouldn't be implementated directly in Swagger-Promote core. It makes more sense to add extra functionilaty using Plugins which can be turned On/Off and configured as needed.  Another benefit is to decouple the Release-Cylce of a Plugin from the Swagger-Promote core. 

Plugins may be used for instance to handle:
- Applications - Swagger-Promote is not supposed to create/manage applications. A plugin might do it
- KPS - A plugin to keep an API in sync with certain KPS entries used by Custom-Policies
- Perform custom validation of the Config-File or Swagger-file
- Adjust the Config- or Swagger-File before it's further processed
- Custom-Error-Handling - A plugin might take in the error-code and send it to somewhere else

Plugins should also help the community to contribute or extend Swagger-Promote more easily accordding to their needs. They are supposed to be very small and should be easily plugged into Swagger-Promote. 

## Plugins implementation details
### Official vs. Private Plugins
- Private plugins are developed by customers and not shared with the community
  - Customers are fully responsible for their own plugins and how to merge them into Swagger-Promote
- Official plugins 
  - are turned __Off__ by default
  - if applicable official plugins are tested as part of the Core-Swagger-Promote integration tests
    - for some, it might be impossible to automatically test them
  - will become part of the official Swagger-Promote release (shipped with the release package)
  - they are stored at Swagger-Promote Github Swagger-Promote `plugins` directory
  - each must have a README.md documenting the Plugin
    - Purpuse, Compatibility with API-Manager, History
  - official plugins must provide a version number (x.y.z)
- Contribution
  - Create a fork of Swagger-Promote
  - Implement the new Plugin, Test it, document it
  - Create a Pull-Request into the develop branch to make this plugin an official plugin
  - A reviewer will review the Plugin
    - also at this point it decided if the Plugin is tested as part of the integration tests
  - If approved, the plugin becomes part of the next release and is tested (if enabled) as part of the integration tests

### Register Plugins
- Standard plugins will be delivered in a extra folder `/plugins` within the main folder
  - A plugin is a JAR-File residing in that folder
    - also extracted Plugins will be supported (`plugins/pluginABC/com/axway/swa.../plugins`)
  - Each plugin has it's own configuration file
    - each plugin can be turned On or Off using that config-file
    - each plugin can have it's own additional config settings in that config file
  - Private plugins can be added by customers by copying them into that directory
  - Swagger-Promote Plugin-Handler will parse the plugin-directory
  - and call each enabled plugin

### Plugin execution
- The Swagger-Promote Plugin-handler will parse the plugin directory looking for config files
- each config file must contain
  - the Class-Name of the Plugin
  - if the Plugin is enabled or / disabled
  - each config file may contain an execution order (e.g. `order=3`)
    - this allows to control the execution order of plugins
    - if no order is given for a plugin it will be executed after all ordered plugins randomly with others unsorted
- if the config is valid, the Plugin is turned on it get's registered into the handler
- According to the registered order all plugins are called by the Swagger-Promote Plugin-Handler at certain steps
  - for that it must implement a `SwaggerPromotePluginInterface`
  1. Just after the API-Config & Swagger-File has been read
    - before the actual API is read from the API-Manager
    - `preProcessDesiredAPI(IAPI desiredAPI)`
  2. After the actual API has been read from the API-Manager
    - `preProcessActualAPI(IAPI desiredAPI, IAPI actualAPI)`
  3. After the Desired- and Actual-API has been compared
    - `preAPIReplication(IAPI desiredAPI, IAPI actualAPI, Changestate)`
  4. Finally, after the API has been replicated
    - `postAPIReplication(IAPI desiredAPI, IAPI actualAPI, Changestate)`
  - in each state a Plugin has the flexibility to modify the Desired- or Actual-API
- An abstract `SwaggerPromoteAbstractPlugin` class is provided and each plugin should inherit from

### Error-Handling
- a Plugin can throw the Exception: PluginException, which is handled by the PluginHandler
  - that execption type can be used to completely abort Swagger-Promote
  - also it can be used to skip the actual plugin, but continue with the next
- in case Swagger-Promote fails, each plugin is called to indicate there is an error
  - `swaggerPromoteFail(DesiredAPI, ActualAPI, AppException)`
  - can be used by a Plugin for further error handling
  - perform some rollback

### Tests / Documentation / Misc
- By default Plugins are not tested as part of the core Swagger-Promote integration tests
  - however, where applicable/useful some of the plugins might be turned on as part of the integration tests
- a Plugin must have some Unit-Tests
  - these tests simulate the behavior when called by Swagger-Promote
  - Unit-Tests are executed by the Swagger-Promote build pipeline
- each plugin must provide a README.md within the plugin folder
- Nice to have: Command `List plugins` to list all available plugins with a short description
