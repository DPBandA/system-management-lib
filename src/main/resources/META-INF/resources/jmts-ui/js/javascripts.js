/* 
 System Management (SM) 
 Copyright (C) 2025  D P Bennett & Associates Limited
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 
 Email: info@dpbennett.com.jm
 */

function checkWhichBrowser() {

    if (navigator.userAgent.toUpperCase().lastIndexOf("MSIE") !== -1) {
        incompatibleBrowserDialog.show();
    }

}

function reloadCurrentPage() {
    connectionErrorAlertDialog.hide();
    window.location.reload();
}

function doLogin() {
    doLoginRequest();
}

function showStatusDialog() {
    PF('statusDialog').show();
}

function hideStatusDialog() {
    PF('statusDialog').hide();
}


