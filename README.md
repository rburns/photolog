# photolog ![](https://travis-ci.org/rburns/photolog.svg?branch=master)
visual journal. current status: resizes and generates metadata for a directory of images

### dependencies

[exiftool](http://www.sno.phy.queensu.ca/~phil/exiftool/) - propbably available in your system's package manager

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

the default properties are _CreateDate, ExposureTime, ScaleFactor35efl, FocalLength, LensType,
Aperture, ISO, Model, GPSPosition, GPSAltitude, ImageWidth, and ImageHeight_. Prop names are those
used by exiftool.

`breakpoints` the breakpoints for resized images

an array of name, width pairs. the default breakpoints are _[["tiny", 200], ["small", 556], ["medium", 804], ["large", 1000]]_

`metadata-format` the format of the output metadata. _transit_, _html_, or _atom_. defaults to transit

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

### output

images in `img-src-dir` are symlinked into `img-out-dir`.  images resized relative to `breakpoints` are written to `img-out-dir`.

metadata describing the images is written to `metadata-path`. in addtion to the keys specified in
`exif-props`, it includes the following keys:

`file-created` file created timestamp

`file-modified` file modified timestamp

`sizes` a list of images derived from `breakpoints`.  `href` and `label` keys for each

`srcset` html srcset attribute value

`height-scale` the height of the image as a multiplier of it's width

`href` the url of the original image prefixed with `href-prefix`

keys are formatted relative to `metatdata-format`. not all keys are present in all formats
