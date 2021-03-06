# jacocoHighlight
**_[Note: This is a work in progress. API is subject to change.]_**

[Download](http://s3.amazonaws.com/nishant-tests-coverage/jacocoHighlight_app.jar "Download Jar")

This is a wrapper of [JaCoCo](http://eclemma.org/jacoco/ "JaCoCo") that expands on its functionalities in making HTML reports. Specifically, it allows for the highlighting of cells to indicate a pass or fail.

[Example](http://s3.amazonaws.com/nishant-tests-coverage/example_report/index.html "JaCoCo Example")  
In the above example, a cell passes if it has 50% or more coverage.

Note that each cell not only reflects the current item's overall coverage, but also the coverage of everything the item contains. So in the above example, a package could have >50% overall coverage, but still be marked as failing because one of its classes is failing. The same applies to classes in terms of its methods. To clarify: For a package to be marked as passing, _every_ class it contains needs to be marked as passing (as well as the package's overall coverage). For a class to be marked as passing, _every_ method it contains needs to meet its coverage criteria (as well as the class' overall coverage).

## Versions
This has only been tested with JaCoCo version 0.7.6

## Usage
### Command Line
You can run the application by using the following command:  
```bash
java -jar $PATH-TO-JAR $PATH-TO-PARAMS
``` 

Where `PATH-TO-PARAMS` is the location of a parameter file for the jacocoHighlight tool (see [Parameter File] (#parameter-file))          


## Parameter File
The parameter file is used to determine the structure of the HTML report generated by the tool. This is done by creating a tree structure of maps and lists in accordance to the [YAML Version 1.1 specs](http://yaml.org/spec/1.1/ "YAML 1.1 Specs"). There are two types of scalars used in this file: [local](#local-scalars) and [global](#global-scalars).

### Structure
The tree structure of the parameter file are composed of two types of nodes: Groups and Bundles. These nodes are maps where each "element" is a scalar that points to a value.  
A Group needs to have a title and a list of children, which may be other Groups or Bundles. Although the list of children may be empty, it will just result in the HTML report containing a group with zero children and zero coverage, making it essentially useless. Therefore, a Group should _never_ be a leaf node.  
A Bundle, on the other hand, is _always_ a leaf node since it never has children. These nodes must have a title as well as a reference to execution data, class files, and source files.

The table below summarizes the required elements of Groups and Bundles:

| Groups                                                | Bundles |
| ----------------------------------------------------- | ------- |
| <ul><li>Title</li><li>Groups and/or Bundles</li></ul> | <ul><li>Title</li><li>Execution Data</li><li>Classes</li><li>Source Files</li></ul> |

Groups and Bundles are automatically detected based on whether the node contains class files or children.

### Local Scalars
Local scalars are used to define properties of a Group or Bundle. Most of these scalars inherit values from their parents when undefined, so in most cases a single definition in the root node would suffice.

| Scalar | Accepted Types | Description | Default |
| --- | --- | --- | --- |
| `title` | <ul><li>`String`</li></ul> | The node's title. | The name of the folder specified by `root` |
| `root`  | <ul><li>`String`</li></ul> | The working directory for this node in relation to the working directory. Uses the parent's `root` if none is specified. | `.` |
| `exec`  | <ul><li>`String`</li><li>`List<String>`</li></ul> | The location of all JaCoCo execution files (`.exec`) to be used by this node, written in relation to `root`. Uses the parent's `exec` if none is specified. Accepts `glob` formatting. | `"*.exec"` |
| `src` | <ul><li>`String`</li><li>`List<String>`</li></ul> | The location of all source files to be used by this node, written in relation to `root`. Uses the parent's `src` if none is specified. Accepts `glob` formatting. | `"**/src/main/java"` |
| `class` | <ul><li>`String`</li><li>`List<String>`</li><li>`Map`</li><li>`List<Map>`</li></ul>| Either a list of locations of class files or a list of Group/Bundle children. Locations are written in relation to `root` and accepts `glob` formatting. | `"**/build/classes/main"`

Note that `class` is used in both Groups and Bundles. If the scalar maps to a `String`/`List<String>` then it is assumed that the node is a Bundle. If it instead maps to another `Map`/`List<Map>`, then the node is assumed to be a Group.

### Global Scalars
Global scalars are used to define other parameters for the jacocoHighlight tool. These scalars should only be defined ***once*** in the ***root node***.

| Scalar | Accepted Types | Description | Default |
| --- | --- | --- | --- | 
| `config` | <ul><li>`String`</li></ul> | The location of the configuration file for the highlighted HTML report in relation to the working directory (see [Configuration File](#configuration-file)). | `null` |
| `out` | <ul><li>`String`</li></ul> | The desired location for the output HTML report in relation to the working directory. | `"out"` |

If `config` is `null` then a report is created without highlighting.

### Examples
Here are some examples of valid parameter files.

#### Basic Report 

```YAML
################
# Basic Report #
################
# Global scalars
config : "config.yml"
out    : "out"

# Local scalars
title  : "Basic Report"
exec   : "*.exec"
src    : "**/src/main/java"
class  : "**/build/classes/main"
```
Alternitavely, since `out`, `exec`, `src`, and `class` default to the same values, an equivalent report can be written like this:

```YAML
########################
# Another Basic Report #
########################

# Global scalars
config : "config.yml"

# Local scalars
title  : "Basic Report"
```

#### Grouping Reports
Grouping reports allow a user to view the overall coverage of a project as well as seeing the coverage of different sections of a project.

```YAML
###################################################
# Making an overall report out of two sub-reports #
###################################################

# Global scalars
config : "config.yml"
out    : "group_report"

# Local scalars
title  : "Basic Reports"
class  :
  -
    title  : "Basic Report - A"
    exec   : "*.exec"
    src    : "**/src/main/java"
    class  : "**/build/classes/main/a"
  -
    title  : "Basic Report - B"
    exec   : "*.exec"
    src    : "**/src/main/java"
    class  : "**/build/classes/main/b"
```

Since `Basic Report - A` and `Basic Report - B` refer to the same pool of execution and source files, we can instead define `exec` and `src` in the parent node.

```YAML
###################################################
# Making an overall report out of two sub-reports #
###################################################

# Global scalars
config : "config.yml"
out    : "group_report"

# Local scalars
title  : "Basic Reports"
exec   : "*.exec"
src    : "**/src/main/java"
class  :
  -
    title  : "Basic Report - A"
    class  : "**/build/classes/main/a"
  -
    title  : "Basic Report - B"
    class  : "**/build/classes/main/b"
```

#### Using `root`
Suppose we have the following file structure:

```
├───coverage_report
│   └───config.yml
├───proj_a
│   ├───data.exec
│   ├───src/main/java
│   └───build/classes/main
└───proj_b
    ├───data.exec
    ├───src/main/java
    └───build/classes/main
```
We would like to create a merged coverage report for `proj_a` and `proj_b` while working from the location of `config.yml`. An implementation of the parameter file for this would then look like...

```YAML
#################################
# Solution without using 'root' #
#################################

# Global scalars
config : "config.yml"
out    : "report"

# Local scalars
title  : "Total Report"
class  :
  -
    title  : "Project A"
    exec   : "../proj_a/*.exec"
    src    : "../proj_a/**/src/main/java"
    class  : "../proj_a/**/build/classes/main"
  -
    title  : "Project B"
    exec   : "../proj_b/*.exec"
    src    : "../proj_b/**/src/main/java"
    class  : "../proj_b/**/build/classes/main"
```
Alternatively, we can use the `root` scalar to determine the working directory for each node and make the file look a bit cleaner.

```YAML
#################################
# Solution with 'root' #
#################################

# Global scalars
config : "config.yml"
out    : "report"

# Local scalars
title  : "Total Report"
class  :
  -
    title  : "Project A"
    root   : "../proj_a"
    exec   : "*.exec"
    src    : "**/src/main/java"
    class  : "**/build/classes/main"
  -
    title  : "Project B"
    root   : "../proj_b"
    exec   : "*.exec"
    src    : "**/src/main/java"
    class  : "**/build/classes/main"
```
We can even continue by applying what was demonstrated in the previous section to make the following equivalent file:

```YAML
#####################################
# Solution with 'root' - simplified #
#####################################

# Global scalars
config : "config.yml"
out    : "report"

# Local scalars
title  : "Total Report"
exec   : "*.exec"
src    : "**/src/main/java"
class  :
  -
    title  : "Project A"
    root   : "../proj_a"
    class  : "**/build/classes/main"
  -
    title  : "Project B"
    root   : "../proj_b"
    class  : "**/build/classes/main"
```

And since `exec`, `src`, and `class` are equivalent to their default values, we can write the file as...

```YAML
##############################################################
# Solution with 'root' - simplified and using default values #
##############################################################

# Global scalars
config : "config.yml"
out    : "report"

# Local scalars
title  : "Total Report"
class  :
  -
    title  : "Project A"
    root   : "../proj_a"
  -
    title  : "Project B"
    root   : "../proj_b"
```



## Configuration File

The configuration file specifies the passing coverage criteria for each item in the report. Like the parameter file, the configuration file is written as a tree structure of Lists and Maps written in accordance to the [YAML Version 1.1 specs](http://yaml.org/spec/1.1/ "YAML 1.1 Specs").  

Each document can accept the following scalars:

| Scalar                    | Description                         |
| ------------------------- | ----------------------------------- |
| [`child`](#child)         | Container for all children nodes    |
| [`id`](#id)          | Name of the node |                                  |
| [`values`](#values)       | Minimum criteria of the hit ratio for each coverage counter       |
| [`propagate`](#propagate) | Whether the criteria should also be applied to children |
| [`type`](#type)           | A specifier of the type of the node |

The configuration file is _cascading_. That is, if two nodes can apply to the same object, then the cofiguration of the later node will override the first.

### `id`
`id` is used to specify all accepted identifying properties of the node. In most cases, defining the `name` field is enough to identify a particular object, but in some specific cases more is needed (such as a method that has the same name as another but with different signatures).

Although there are many fields that can be defined with `id`, some are only applicable to nodes of certain types. For example, a node with `id` containing a `superclass` definition but also having a `package` type will not match anything since packages don't have superclasses.

All scalars in `id` support wildcard characters `*` and `?`.

| Scalar        | Type      | Description        | Default | Applicable Types |
| ------------- | :-------: | -------------------| :-----: | :------: |
| `name`        | `String`  | Object name        | `*`     | `any`    |
| `signature`   | `String`  | Object signature   | `*`     | <ul><li>`class`</li><li>`method`</li></ul> |
| `superclass`  | `String`  | Object superclass  | `*`     | `class`  |
| `description` | `String`  | Object description | `*`     | `method` |

If `id` is mapped to a String, then that String will be interpreted as the value for the `name` scalar.

### `type`
This scalar specifies the type of object a node should match to. `type` should map to a String.

The accepted values are:

| Name      | Description                                    |
| --------- | ---------------------------------------------- |
| `group`   | Matches the node only to [groups](#structure)  |
| `bundle`  | Matches the node only to [bundles](#structure) |
| `package` | Matches the node only to packages              |
| `class`   | Matches the node only to classes               |
| `method`  | Matches the node only to methods               |
| `any`     | Matches the node to any type                   |

By default `type` is mapped to `any`.

### `child`
`child` acts as a container of all the children of a node. Thus, this scalar maps to a map that represents a node or a list of maps that each represent a different node. A child node acts the same as a top-level node with the exception that this will only match objects whose parent matches the parent node.

The `child` scalar does **not** enforce a node to match if the object has a particular child, but for its children to match only if the object has a particular parent. Thus a node will match an object even if their children do not match.

### `values`

| Scalar        | Type          | Description                           |
| ------------- | :-----------: | --------------------------------------|
| `instruction` | `int | float` | Minimum instruction hit percentage    |
| `branch`      | `int | float` | Minimum branch hit percentage         |
| `complexity`  | `int | float` | Minimum complexity hit percentage     |
| `line`        | `int | float` | Minimum line hit percentage           |
| `method`      | `int | float` | Minimum method hit percentage         |
| `class`       | `int | float` | Minimum class hit percentage          |

If `values` is mapped to a number, then every scalar `values` can contain will be set to 50. By default `values` is set to `0`.

### `propagate`
`propagate` is a boolean value. If `true`, then whatever criteria is applied to an object will also be recursively applied to every child it contains. By default `propagate` is set to `false`.


### Examples

```
# Require every object in the report to have a minimum coverage of 50%
values    : 50
propagate : true
```


## To Do
- Tests!
- Clean up!
