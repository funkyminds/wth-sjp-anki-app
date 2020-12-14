# wth-sjp-anki-app

#### This project is under heavy development.

This is a full application that integrates:
- [sjp.pwn.pl](https://sjp.pwn.pl/) as a translation service
- [anki](https://apps.ankiweb.net/) as a flashcard/repository service

Configurable via a [zio-config](https://zio.github.io/zio-config/).

Configuration file template:
```json
{
  words_path: "path to file with phrases to be queried",
  anki: {
    deck_name: "name::of::anki::deck::to:be::populated"
  },
  pons: {
    source: "en",
    target: "es",
    secret: "access token from pons.eu"
  }
}

```

More to come.