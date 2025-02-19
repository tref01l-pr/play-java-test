#!/bin/bash
exec ./bin/play-java-seed \
  -Dconfig.file=conf/application.conf \
  -Dplay.http.secret.key="$PLAY_SECRET_KEY"