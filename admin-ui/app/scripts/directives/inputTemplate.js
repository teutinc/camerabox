'use strict';

angular.module('photoboothAdminApp')
    .directive('cbInputTemplate', [function() {
        return {
            restrict: 'AE',
            scope: {
                template: '=ngModel',
                contents: '=contents'
            },
            templateUrl: 'scripts/directives/inputTemplate.html',
            link: function(scope) {
                scope.availableTemplateTypes = ['singleText', 'singleImage', 'textAndImage'];
            }
        };
    }]);