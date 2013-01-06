module.exports = function(grunt) {

  grunt.loadNpmTasks('grunt-grunticon');

  grunt.initConfig({
    grunticon: {
      src: "icons/",
      dest: "stylesheets/icons/"
    }
  });

  grunt.registerTask('default', 'grunticon');

};

