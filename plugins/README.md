## Plugins for Swagger-Promote

Swagger-Promote is already very feature rich and provides a lot of flexibility. However, some of the enhancement requests
shouldn't be implementated directly in Swagger-Promote core. It makes more sense to add extra functionilaty using Plugins which can
be turned On/Off and configured as needed.  

Plugins may be used for instance to handle:
- Applications - Swagger-Promote is not supposed to create/manage applications. A plugin might do it
- Update KPS entries - A plugin to keep an API in sync with certain KPS entries used by Custom-Policies
- Perform custom validation of the Config-File or Swagger-file
- Adjust the Config- or Swagger-File before it's further processed

Plugins should also help the community to better contribute or extend Swagger-Promote to their needs, as they are supposed
to be very small and should be easily plugged into Swagger-Promote

### Plugins implementation details
#### Register Plugins
- Standard plugins will be delivered in a extra folder `/plugins` within the main folder
  - A plugin is a JAR-File residing in that folder
    - also
  - Each plugin has it's own configuration file
    - each plugin can be turned On or Off using that config-file
    - each plugin can have it's own additional config settings in that confi file
  - Extra/Custom plugins can be added by customers by copying into that directory
  - Swagger-Promote Plugin-Handler will parse the plugin-directory
  - and call each enabled plugin

#### Plugin execution
- The Swagger-Promote Plugin-handler will parse the plugin directory looking for config files
- each config file must contain
  - the Class-Name of the Plugin
  - if the Plugin is enabled or / disabled
  - each config file may contain an execution order (e.g. `order=3`)
    - this allows to control the execution order of plugins
    - if no order is given for a plugin it will be executed after all others randomly with others
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

#### Error-Handling
- a Plugin can throw the Exception: PluginException, which is handled by the PluginHandler
  - that execption type can be used to completely abort Swagger-Promote
  - also it can be used to skip the actual plugin, but continue with the next

#### Tests / Documentation / Misc
- a Plugin must have some Unit-Tests
  - these tests simulate the behavior when called by Swagger-Promote
- each plugin must provide a README.md within the plugin folder
- each plugin must have a version number in that format (x.y.z)
- Nice to have: Command `List plugins` to list all available plugins with a short description
