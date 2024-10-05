/*
 * DpdToolBarCom.java
 *
 * Copyright (C) 2023-2024 J. R. Bhaddacak 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.dpd;

import paliplatform.base.*;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/** 
 * The tool bar component for DPD module.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */
public class DpdToolBarCom extends HBox {
	public DpdToolBarCom() {
		final Button dictButton = new Button("", new TextIcon("dpd", TextIcon.IconSet.CUSTOM));
		dictButton.setTooltip(new Tooltip("DPD Dictionary"));
		dictButton.setOnAction(actionEvent -> DpdDictWin.INSTANCE.display());
		getChildren().addAll(dictButton);
	}

}

