/*
 * Copyright (c) 2016 Villu Ruusmann
 *
 * This file is part of JPMML-SkLearn
 *
 * JPMML-SkLearn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SkLearn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SkLearn.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.sklearn;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Expression;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.Model;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.jpmml.converter.Feature;
import org.jpmml.converter.ModelEncoder;
import org.jpmml.converter.WildcardFeature;
import org.jpmml.model.visitors.FieldRenamer;
import sklearn.Transformer;

public class SkLearnEncoder extends ModelEncoder {

	private Map<FieldName, FieldName> renamedFields = new LinkedHashMap<>();


	@Override
	public PMML encodePMML(Model model){
		PMML pmml = super.encodePMML(model);

		Collection<Map.Entry<FieldName, FieldName>> entries = this.renamedFields.entrySet();
		for(Map.Entry<FieldName, FieldName> entry : entries){
			FieldRenamer renamer = new FieldRenamer(entry.getKey(), entry.getValue());

			renamer.applyTo(pmml);
		}

		return pmml;
	}

	public void updateFeatures(List<Feature> features, Transformer transformer){
		OpType opType;
		DataType dataType;

		try {
			opType = transformer.getOpType();
			dataType = transformer.getDataType();
		} catch(UnsupportedOperationException uoe){
			return;
		}

		for(Feature feature : features){

			if(feature instanceof WildcardFeature){
				WildcardFeature wildcardFeature = (WildcardFeature)feature;

				updateType(wildcardFeature.getName(), opType, dataType);
			}
		}
	}

	public void updateType(FieldName name, OpType opType, DataType dataType){
		DataField dataField = getDataField(name);

		if(dataField == null){
			throw new IllegalArgumentException(name.getValue());
		}

		dataField.setOpType(opType);
		dataField.setDataType(dataType);
	}

	public DataField createDataField(FieldName name){
		return createDataField(name, OpType.CONTINUOUS, DataType.DOUBLE);
	}

	public DerivedField createDerivedField(FieldName name, Expression expression){
		return createDerivedField(name, OpType.CONTINUOUS, DataType.DOUBLE, expression);
	}

	public void renameField(FieldName from, FieldName to){
		this.renamedFields.put(from, to);
	}
}