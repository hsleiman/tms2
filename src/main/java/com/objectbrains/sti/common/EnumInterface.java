/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.common;

/**
 *
 * @author raine.cabal
 * @param <T>
 */
public interface EnumInterface<T extends Enum<T>> {
    
    int getId();
    
}

