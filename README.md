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

`metadata-format` the format of the output metadata. _transit_, or _html_. defaults to transit

`html-tmpl` the path of the template to be used for html output. _##PHOTOS##_ will be replaced with metadata

### running

```
node ./process/main.js /path/to/config
```
