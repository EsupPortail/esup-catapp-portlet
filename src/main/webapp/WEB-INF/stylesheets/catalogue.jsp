<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0" %>
<%@ taglib prefix="rs" uri="http://www.jasig.org/resource-server" %>
<rs:aggregatedResources path="/resources.xml"/>


<script type="text/javascript">
    angular.element(document).ready(function () {
        catAppPortlet("<portlet:namespace/>", "${resourceURL}", "${actionURL}", "${containerURL}", "${withFavs}");
        angular.bootstrap(angular.element(document.getElementById("catAppPortlet-<portlet:namespace/>")), ["<portlet:namespace/>"]);
    });
</script>
<div id="catAppPortlet-<portlet:namespace/>" class="portlet-container catapp" ng-view>

    <div ng-controller="AppList2Ctrl" ng-cloak class="container">
        <div class="alertContainer">
            <div class="alert alert-info" ng-repeat="alert in alerts">
                {{alert.msg}}
            </div>
        </div>
        <div class="tabsContainer" ng-switch on="withFavs">
            <div ng-switch-when="true">
                <tabset>
                    <tab>
                        <tab-heading>
                            Favoris
                        </tab-heading>
                        <div class="panel-body">
                            <div id="dropdown-Menu-<portlet:namespace/>">
                                <ng-switch on="applications.length > 0">
                                    <div class="ui-state-disabled" ng-switch-when="false">
                                        S&eacute;lectionner les applications dans le catalogue pour les ajouter &agrave; la liste des favoris
                                    </div>
                                </ng-switch>
                                <ul class="list-group" ui-sortable="sortableOptions" ng-model="applications">
                                    <li ng-repeat="application in applications" class="list-group-item list-group-item-info">
                                        <div class="handle">
                                            <a ng-click="enableEdit(application)" href="#" title="Description de l'application" class="help"><i class="fa fa-lg fa-info-circle pull-left"></i></a>
                                            <a ng-click="delFromFav($index)" href="#" title="Retirer des favoris"><i class="fa fa-lg fa-star text-warning pull-left"></i></a>
                                            <div class="pull-right">
                                                <i class="fa fa-lg fa-sort"></i>
                                            </div>
                                        </div>
                                        <div ng-switch="{{application.state}}" class="cat-appli">
                                            <div ng-switch-when="true">
                                                <span class="unavailable">{{application.title}}</span>
                                                <div ng-show="application.editable" class="desc-appli offtree">
                                                    <div class="popover-title">Description de l'application
                                                        <div class="pull-right">
                                                            <a ng-click="disableEdit(application)" href="#"><i class="fa fa-lg fa-times"></i></a>
                                                        </div>
                                                    </div>
                                                    <div class="popover-content">Vous n'avez pas les droits suffisants pour acc&eacute;der &agrave; cette application</div>
                                                </div>
                                            </div>

                                            <div ng-switch-when="false">
                                                <div ng-switch="application.acces">

                                                    <div ng-switch-when="Activated">
                                                        <a href="{{application.url}}" target="{{application.target}}">{{application.title}}</a>
                                                        <div ng-show="application.editable" class="desc-appli offtree">
                                                            <div class="popover-title">Description de l'application
                                                                <div  class="pull-right">
                                                                    <a  ng-click="disableEdit(application)" href="#"><i class="fa fa-lg fa-times"></i></a>
                                                                </div>
                                                            </div>
                                                            <div class="popover-content"><span ng-bind-html="application.description"></span></div>
                                                        </div>
                                                    </div>

                                                    <div ng-switch-when="Deactivated">
                                                        <span class="unavailable" ng-click="callTooltip($event)">{{application.title}}</span>
                                                        <div ng-show="application.editable" class="desc-appli offtree">
                                                            <div class="popover-title">Description de l'application
                                                                <div class="pull-right">
                                                                    <a ng-click="disableEdit(application)" href="#"><i class="fa fa-lg fa-times"></i></a>
                                                                </div>
                                                            </div>
                                                            <div class="popover-content">
                                                                <span class="info-box">L'application est provisoirement indisponible</span>
                                                                <span ng-bind-html="application.description"></span>
                                                            </div>
                                                        </div>
                                                    </div>

                                                </div>
                                            </div>

                                        </div>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </tab>
                    <tab>
                        <tab-heading>
                            Catalogue
                        </tab-heading>
                        <div class="panel-body" ng-animate="'animate'">
                            <div class="panel panel-heading">
                                Rechercher : <input type="text" ng-model="searchText"/>
                            </div>

                            <div class="column">

                                <div ng-show="searchText && searchText.length >= 2">
                                    <span class="result-title">R&eacute;sultat de la recherche : </span>
                                    <ul>
                                        <li ng-repeat="application in allApplications | filter:searchText" class="list-group-item list-group-item-info">
                                            <a ng-click="enableEdit(application)" href="#" title="Description de l'application" class="help"><i class="fa fa-lg fa-info-circle pull-left"></i></a>
                                            <a ng-click="addFav(application)" href="#" title="Ajouter aux favoris">
                                                <i ng-class="{'fa-star-o':prefs.indexOf(application.code)== -1,
                                            'fa-star':prefs.indexOf(application.code)> -1}"
                                                   class="fa fa-lg text-warning pull-left"></i>
                                            </a>
                                            <a href="{{application.url}}" target="{{application.target}}">{{application.title}}</a>
                                            <div ng-show="application.editable" class="desc-appli offtree">
                                                <div class="popover-title">Description de l'application
                                                    <div class="pull-right">
                                                        <a ng-click="disableEdit(application)" href="#"><i class="fa fa-lg fa-times"></i></a>
                                                    </div>
                                                </div>
                                                <div class="popover-content"><span ng-bind-html="application.description"></span>
                                                </div>
                                            </div>
                                        </li>
                                    </ul>
                                </div>

                                <div id="sidebar-<portlet:namespace/>" class="sidebar" ng-hide="searchText && searchText.length >= 2">
                                    <span>{{domaines.domain.length}}</span>
                                    <div ng-switch="domaines.domain.caption.length>0">
                                        <div ng-switch-when="true">
                                            <ul>
                                                <li>
                                                    <i class="fa fa-caret-right pull-left" style="float:left"></i>
                                                    <a href="javascript:void(0)">{{domaines.domain.caption}}</a>
                                                    <ul class="sub-menu">
                                                        <li ng-repeat="domaine in domaines.domain.applications">
                                                            <application member="domaine" add-fav="addFav" enable-edit="enableEdit" disable-edit="disableEdit" prefs="prefs" get-target="getTarget"></application></li>
                                                        <li ng-repeat="domaine in domaines.subDomains">
                                                            <domaine member="domaine" add-fav="addFav" enable-edit="enableEdit" disable-edit="disableEdit" prefs="prefs" get-target="getTarget"></domaine></li>
                                                    </ul>
                                                </li>
                                            </ul>
                                        </div>
                                        <div class="ui-state-disabled" ng-switch-when="false">
                                            La liste des domaines et applications autoris&eacute;s est vide.
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </tab>
                </tabset>
            </div>
            <div ng-switch-when="false">
                <div class="panel-body" ng-animate="'animate'">
                    <div class="panel panel-heading">
                        Rechercher : <input type="text" ng-model="searchText"/>
                    </div>

                    <div class="column">

                        <div ng-show="searchText && searchText.length >= 2">
                            <span class="result-title">R&eacute;sultat de la recherche : </span>
                            <ul>
                                <li ng-repeat="application in allApplications | filter:searchText" class="list-group-item list-group-item-info">
                                    <a ng-click="enableEdit(application)" href="#" title="Description de l'application" class="help"><i class="fa fa-lg fa-info-circle pull-left"></i></a>
                                    <a href="{{application.url}}" target="{{application.target}}">{{application.title}}</a>
                                    <div ng-show="application.editable" class="desc-appli offtree">
                                        <div class="popover-title">Description de l'application
                                            <div class="pull-right">
                                                <a ng-click="disableEdit(application)" href="#"><i class="fa fa-lg fa-times"></i></a>
                                            </div>
                                        </div>
                                        <div class="popover-content"><span ng-bind-html="application.description"></span>
                                        </div>
                                    </div>
                                </li>
                            </ul>
                        </div>

                        <div id="sidebar-<portlet:namespace/>" class="sidebar" ng-hide="searchText && searchText.length >= 2">
                            <span>{{domaines.domain.length}}</span>
                            <div ng-switch="domaines.domain.caption.length>0">
                                <div ng-switch-when="true">
                                    <ul>
                                        <li>
                                            <i class="fa fa-caret-right pull-left" style="float:left"></i>
                                            <a href="javascript:void(0)">{{domaines.domain.caption}}</a>
                                            <ul class="sub-menu">
                                                <li ng-repeat="domaine in domaines.domain.applications">
                                                    <application member="domaine" add-fav="addFav" enable-edit="enableEdit" disable-edit="disableEdit" prefs="prefs" get-target="getTarget"></application></li>
                                                <li ng-repeat="domaine in domaines.subDomains">
                                                    <domaine member="domaine" add-fav="addFav" enable-edit="enableEdit" disable-edit="disableEdit" prefs="prefs" get-target="getTarget"></domaine></li>
                                            </ul>
                                        </li>
                                    </ul>
                                </div>
                                <div class="ui-state-disabled" ng-switch-when="false">
                                    La liste des domaines et applications autoris&eacute;s est vide.
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        function launchJs() {

            var menu = document.createElement('script');
            menu.type = 'text/javascript';
            menu.innerHTML = htmlString;
            document.getElementById('catAppPortlet-<portlet:namespace/>').appendChild(menu);
        }
        setTimeout("launchJs();", 2000)


        var htmlString = "$(document).ready(function() {";

        htmlString += "$('#sidebar-<portlet:namespace/> ul li a').click(function(ev) {";
        htmlString += "$('#sidebar-<portlet:namespace/> .sub-menu').not($(this).parents('.sub-menu')).slideUp();";
        htmlString += "$(this).next('.sub-menu').slideToggle();";
        htmlString += "ev.stopPropagation();";
        htmlString += "});";
        htmlString += "$('.menudrop li a').click(function(ev) {";
        htmlString += "$(this).next('.dropdown').fadeToggle('slow');";
        htmlString += "});";
        htmlString += "$('#sidebar-<portlet:namespace/> ul > li:first-child > a').next('.sub-menu').slideToggle();";

        htmlString += "});";
    </script>
</div>
