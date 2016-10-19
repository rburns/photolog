# photolog
visual journal. current status: resizes and generates metadata for a directory of images

### building

```npm run process-prod```

### developing

```npm run process-dev```

### clean

```npm run clean```

### create a config file

it's JSON

#### required keys

`img-src-dir` the path of the _directory_ containing your images

`img-out-dir` the path of the _directory_ to write all your resized images

`metadata-path` the path to the _file_ in which to write the image metadata

`href-prefix` the string with which to prefix all image paths

#### optional keys

`exif-props` a list of exif properites to include in the output metadata

the default properties are _CreateDate, ExposureTime, ScaleFactor35efl, FocalLength, LensType, Aperture, ISO, Model, ImageWidth, and ImageHeight_

`breakpoints` the breakpoints for resized images

an array of name, width pairs. the default breakpoints are _[["tiny", 200], ["small", 556], ["medium", 804], ["large", 1000]]_

`metadata-format` the format of the output metadata. _transit_, or _html_. defaults to transit

`html-tmpl` the path of the template to be used for html output.

there is no default value. when _metadata-format_ is _html_, this key is required. the string _##PHOTOS##_ in the template will be replaced with image metadata

`exif-transforms` arbitrary transformations of exif values

described as an array of _key_, _value_, _new-value_ triplets. example use: cleaning up camera
names.

```
"exif-transforms": [
    ["model", "Digimax A6", "Samsung Digimax A6"],
    ["model", "FE190/X750", "Olympus FE-190"],
]
```

### running

```
node ./process/main.js /path/to/config
```
