# WHAT
    A module plugin for generate module by template.
    Reduce boilerplate routine in multi-module gradle projects.



![image-20210610075844098](https://raw.githubusercontent.com/jacky1234/picArchieve/master/uPic/image-20210610075844098.png)



## Usage

To make it works, follow these steps:
> 1. Create a directory called <em>gradle_templates</em> in the root of your project.
> 2. Create subdirectories in that folder. Their names don't matter.
> 3. Inside of each folder create a <em>config.yml</em> file and define a name of your module. A template of config.yml can be seen below root project. The file name is config.yml.tl
> 4. Also, create additional folders **static** and **template** and put your files in them.
s
**Config.yml**

You can add configuration in this file. Take the following as examples:

```yaml
name: "module-${module.baseName}"

genDir: "modules"

directories:
  - "src/main/res/layout/drawable"
  - "src/main/res/layout/drawable-xhdpi"
  - "src/main/res/layout/drawable-xxhdpi"
  - "src/main/res/layout/drawable-xxxhdpi"
  - "src/main/res/values"
```

The key explain as follows:

- name : the module's name
- genDir :  the module will be generated
- directories : the dirs to be generated