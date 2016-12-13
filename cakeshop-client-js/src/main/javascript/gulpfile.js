
var gulp        = require("gulp");
var gutil       = require("gulp-util");
var concatenate = require("gulp-concat");
var minify      = require("gulp-uglify");
var bower       = require("main-bower-files");
var watch       = require("gulp-watch");
var batch       = require("gulp-batch");
var plumber     = require("gulp-plumber");

// write compiled dist files into `target/dist`
var dest = "../../../target/dist/";
var libs = "lib/**/*.js";

gulp.task("default", ["compile"]);

gulp.task("compile-libs", function() {
  return gulp.src(libs)
    .pipe(concatenate("cakeshop.js"))
    .pipe(gulp.dest(dest))
    // .pipe(gulp.dest("../../../../cakeshop-api/src/main/webapp/js/vendor/"))
    .pipe(concatenate("cakeshop-min.js"))
    .pipe(minify())
    .pipe(gulp.dest(dest));
});

gulp.task("compile-combined", function() {
  return gulp.src(bower().concat(libs))
    .pipe(concatenate("cakeshop-combined.js"))
    .pipe(gulp.dest(dest))
    .pipe(concatenate("cakeshop-combined-min.js"))
    .pipe(minify())
    .pipe(gulp.dest(dest));
});

gulp.task("compile", ["compile-libs", "compile-combined"]);

gulp.task("install", ["compile"], function() {
  return gulp.src(dest+"cakeshop.js")
    // .pipe(concatenate("cakeshop.js"))
    .pipe(gulp.dest("../../../../cakeshop-api/src/main/webapp/js/vendor/"));
});

gulp.task("watch", ["install"], function() {
  watch("lib/**/*.js", batch(function (events, done) {
    gutil.log("-- recompiling on file change --");
    gulp.start("install", done);
  }));
});
