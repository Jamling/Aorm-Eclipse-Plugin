/*
 * Copyright 2012 Jamling(li.jamling@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ieclipse.aorm.eclipse.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author melord
 * 
 */
public class ComponentIntentFilter {
    String name;
    List<String> categories = new ArrayList<String>();
    List<String> actions = new ArrayList<String>();
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the categories
     */
    public List<String> getCategories() {
        return categories;
    }
    
    /**
     * @param categories
     *            the categories to set
     */
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
    
    /**
     * @return the actions
     */
    public List<String> getActions() {
        return actions;
    }
    
    /**
     * @param actions
     *            the actions to set
     */
    public void setActions(List<String> actions) {
        this.actions = actions;
    }
    
}
