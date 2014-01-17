/*
 * Copyright 2012-2013 Canoo Engineering AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.saxsys.hackathon.marie.baerschen.dolphin;

import javafx.application.Application;

import org.opendolphin.core.client.ClientDolphin;
import org.opendolphin.core.client.ClientModelStore;
import org.opendolphin.core.client.comm.BlindCommandBatcher;
import org.opendolphin.core.client.comm.HttpClientConnector;
import org.opendolphin.core.client.comm.JavaFXUiThreadHandler;
import org.opendolphin.core.comm.JsonCodec;

public class JavaChatDemo {

	public static void main(String[] args) throws Exception {
		ClientDolphin clientDolphin = new ClientDolphin();
		clientDolphin.setClientModelStore(new ClientModelStore(clientDolphin));
		BlindCommandBatcher batcher = new BlindCommandBatcher();
		batcher.setMergeValueChanges(true);
		HttpClientConnector connector = new HttpClientConnector(clientDolphin,
				batcher, "https://klondike.canoo.com/dolphin-grails/dolphin/");
		connector.setCodec(new JsonCodec());
		connector.setUiThreadHandler(new JavaFXUiThreadHandler());
		clientDolphin.setClientConnector(connector);

		ChatApplication.clientDolphin = clientDolphin;
		Application.launch(ChatApplication.class);
	}

}
