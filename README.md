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

<p class="description">the default properties are _CreateDate, ExposureTime, ScaleFactor35efl, FocalLength, LensType, Aperture, ISO, Model, ImageWidth, and ImageHeight_</p>

`breakpoints` the breakpoints for resized images

<p class="description">an array of name, width pairs. the default breakpoints are _[["tiny", 200],
["small", 556], ["medium", 804], ["large", 1000]]_</p>

`html-tmpl` the path of the template to be used for html output.

<p class="description">there is no default value. the string _##PHOTOS##_ in the template will be
replaced with image metadata</p>

`metadata-format` the format of the output metadata. _transit_, or _html_. defaults to transit

### running

```
node ./process/main.js /path/to/config
```

<style>
.description { font-size: 15px; padding-left: 1.5em; }
</style>
