# nablarch-biz-sample-all

| master | develop |
|:-----------|:------------|
|[![Build Status](https://travis-ci.org/nablarch/nablarch-biz-sample-all.svg?branch=master)](https://travis-ci.org/nablarch/nablarch-biz-sample-all)|[![Build Status](https://travis-ci.org/nablarch/nablarch-biz-sample-all.svg?branch=develop)](https://travis-ci.org/nablarch/nablarch-biz-sample-all)|

## 依存ライブラリ

本モジュールのコンパイルまたはテストには、下記ライブラリを手動でローカルリポジトリへインストールする必要があります。

ライブラリ          |ファイル名       |グループID     |アーティファクトID   |バージョン   |
:-------------------|:----------------|:--------------|:--------------------|:------------|
[kaptcha](https://code.google.com/archive/p/kaptcha/downloads) |kaptcha-2.3.2.jar |com.google.code |kaptcha              |2.3.2        |


上記ライブラリは、下記コマンドでインストールしてください。


```
mvn install:install-file -Dfile=<ファイル名> -DgroupId=<グループID> -DartifactId=<アーティファクトID> -Dversion=<バージョン> -Dpackaging=jar
```

