'use strict';

angular.module('cameraboxAdminApp')
    .directive('cbInputTemplate', [function() {
        return {
            restrict: 'AE',
            scope: {
                template: '=ngModel',
                contents: '=contents'
            },
            templateUrl: 'views/directives/inputTemplate.html',
            link: function(scope) {
                scope.availableTemplateTypes = ['singleText', 'singleImage', 'textAndImage'];
            }
        };
    }]);