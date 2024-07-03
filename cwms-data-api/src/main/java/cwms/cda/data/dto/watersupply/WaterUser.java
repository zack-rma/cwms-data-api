/*
 *
 *  MIT License
 *
 *  Copyright (c) 2024 Hydrologic Engineering Center
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *
 */

package cwms.cda.data.dto.watersupply;

import cwms.cda.api.errors.FieldException;
import cwms.cda.data.dto.CwmsDTOBase;
import cwms.cda.data.dto.CwmsId;

public class WaterUser implements CwmsDTOBase {

    private String entityName;
    private CwmsId parentLocationRef;
    private String waterRight;

    private WaterUser() {
    }

    public WaterUser(String entityName, CwmsId locationRef, String waterRight) {
        this.entityName = entityName;
        this.parentLocationRef = locationRef;
        this.waterRight = waterRight;
    }

    public CwmsId getParentLocationRef() {
        return this.parentLocationRef;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public String getWaterRight() {
        return this.waterRight;
    }

    @Override
    public void validate() throws FieldException {
        if (this.entityName == null || this.entityName.isEmpty()){
            throw new FieldException("Entity Name cannot be null or empty");
        }
        if (this.waterRight == null || this.waterRight.isEmpty()){
            throw new FieldException("Water Right cannot be null or empty");
        }
        this.parentLocationRef.validate();
    }
}
