'use strict';

angular.module('cameraboxAdminApp')
    .directive('cbImageInput', ['$location', 'FileUploader', 'DefaultPort', function($location, FileUploader, DefaultPort) {
        return {
            restrict: 'AE',
            scope: {
                image: '=ngModel',
                contents: '=contents'
            },
            templateUrl: 'views/directives/imageInput.html',
            compile: function () {
                return {
                    pre: function (scope) {
                        scope.uploader = new FileUploader({
                            url: 'http://' + $location.host() + ':' + DefaultPort + '/api/upload',
                            autoUpload: true,
                            onSuccessItem: function (item, response, status, headers) {
                                scope.contents.push(item.file.name);
                                scope.image = item.file.name;
                                scope.uploadSuccess = item.file.name + ' uploaded successfully';
                                scope.uploadError = '';
                            },
                            onErrorItem: function (item, response, status, headers) {
                                scope.uploadSuccess = '';
                                scope.uploadError = 'unable to upload the file: ' + item.file.name + ' => ' + response;
                            }
                        });
                    }
                }
            }
        }
    }]);

