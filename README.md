# photolog
visual journal. current status: resizes and generates metadata for a directory of images.

### building

```npm run process-prod```

### developing

```npm run process-dev```

### clean

```npm run clean```

### running

create a config file.

```json
{
  "img-src-dir": "/path/to/your/images",
  "img-out-dir": "/path/to/write/images",
  "metadata-path": "/path/to/write/img-metadata.json",
  "href-prefix": "/eg/webroot/relative/path/to/images"
}
```

ensure software has been built. then run.

```
node ./process/main.js /path/to/config
```
