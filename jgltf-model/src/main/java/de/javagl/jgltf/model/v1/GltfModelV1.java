/*
 * www.javagl.de - JglTF
 *
 * Copyright 2015-2016 Marco Hutter - http://www.javagl.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.jgltf.model.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.logging.Logger;

import de.javagl.jgltf.impl.v1.Accessor;
import de.javagl.jgltf.impl.v1.Buffer;
import de.javagl.jgltf.impl.v1.GlTF;
import de.javagl.jgltf.impl.v1.Image;
import de.javagl.jgltf.impl.v1.Program;
import de.javagl.jgltf.impl.v1.Shader;
import de.javagl.jgltf.impl.v1.Technique;
import de.javagl.jgltf.impl.v1.Texture;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.BufferModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.ImageModel;
import de.javagl.jgltf.model.TextureModel;
import de.javagl.jgltf.model.gl.ProgramModel;
import de.javagl.jgltf.model.gl.ShaderModel;
import de.javagl.jgltf.model.gl.TechniqueModel;
import de.javagl.jgltf.model.gl.impl.DefaultProgramModel;
import de.javagl.jgltf.model.gl.impl.DefaultShaderModel;
import de.javagl.jgltf.model.gl.impl.DefaultTechniqueModel;
import de.javagl.jgltf.model.impl.DefaultGltfModel;
import de.javagl.jgltf.model.io.v1.GltfAssetV1;

/**
 * Implementation of a {@link GltfModel}, based on a {@link GlTF glTF 1.0}.<br>
 */
public final class GltfModelV1 extends DefaultGltfModel implements GltfModel
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(GltfModelV1.class.getName());

    /**
     * The {@link IndexMappingSet}
     */
    private IndexMappingSet indexMappingSet;
    
    /**
     * The {@link ShaderModel} instances that have been created from
     * the {@link Shader} instances
     */
    private final List<DefaultShaderModel> shaderModels;

    /**
     * The {@link ProgramModel} instances that have been created from
     * the {@link Program} instances
     */
    private final List<DefaultProgramModel> programModels;
    
    /**
     * The {@link TechniqueModel} instances that have been created from
     * the {@link Technique} instances
     */
    private final List<DefaultTechniqueModel> techniqueModels;

    /**
     * Creates a new model for the given glTF
     * 
     * @param gltfAsset The {@link GltfAssetV1}
     */
    public GltfModelV1(GltfAssetV1 gltfAsset)
    {
        Objects.requireNonNull(gltfAsset, 
            "The gltfAsset may not be null");
        setGltf(gltfAsset.getGltf());

        this.shaderModels = new ArrayList<DefaultShaderModel>();
        this.programModels = new ArrayList<DefaultProgramModel>();
        this.techniqueModels = new ArrayList<DefaultTechniqueModel>();
        
        this.indexMappingSet = IndexMappingSets.create((GlTF) getGltf());
        GltfModelCreatorV1 gltfModelCreatorV1 = 
            new GltfModelCreatorV1(gltfAsset, this, indexMappingSet);
        gltfModelCreatorV1.create();
    }
    
    /**
     * Returns the {@link BufferModel} for the {@link Buffer} with the given ID.
     * If the given ID is not valid, then a warning will be printed and 
     * <code>null</code> will be returned.
     *  
     * @param bufferId The {@link Buffer} ID
     * @return The {@link BufferModel}
     */
    public BufferModel getBufferModelById(String bufferId)
    {
        return get("buffers", bufferId, this::getBufferModel);
    }
    
    /**
     * Returns the {@link ShaderModel} for the {@link Shader} with the given ID.
     * If the given ID is not valid, then a warning will be printed and 
     * <code>null</code> will be returned.
     *  
     * @param shaderId The {@link Shader} ID
     * @return The {@link ShaderModel}
     */
    public ShaderModel getShaderModelById(String shaderId)
    {
        return get("shaders", shaderId, this::getShaderModel);
    }

    /**
     * Returns the {@link ImageModel} for the {@link Image} with the given ID.
     * If the given ID is not valid, then a warning will be printed and 
     * <code>null</code> will be returned.
     *  
     * @param imageId The {@link Image} ID
     * @return The {@link ImageModel}
     */
    public ImageModel getImageModelById(String imageId)
    {
        return get("images", imageId, this::getImageModel);
    }
    
    /**
     * Returns the {@link TextureModel} for the {@link Texture} with the given
     * ID. If the given ID is not valid, then a warning will be printed and 
     * <code>null</code> will be returned.<br>
     * <br>
     * This is only used for supporting the legacy technique-based rendering.
     *  
     * @param textureId The {@link Texture} ID
     * @return The {@link TextureModel}
     */
    public TextureModel getTextureModelById(String textureId)
    {
        return get("textures", textureId, this::getTextureModel);
    }
    
    /**
     * Returns the {@link AccessorModel} for the {@link Accessor} with the 
     * given ID.
     * If the given ID is not valid, then a warning will be printed and 
     * <code>null</code> will be returned.
     *  
     * @param accessorId The {@link Accessor} ID
     * @return The {@link AccessorModel}
     */
    public AccessorModel getAccessorModelById(String accessorId)
    {
        return get("accessors", accessorId, this::getAccessorModel);
    }
    
    /**
     * Add the given {@link ShaderModel} to this model
     * 
     * @param shaderModel The instance to add
     */
    public void addShaderModel(DefaultShaderModel shaderModel)
    {
        shaderModels.add(shaderModel);
    }

    /**
     * Remove the given {@link ShaderModel} from this model
     * 
     * @param shaderModel The instance to remove
     */
    public void removeShaderModel(DefaultShaderModel shaderModel)
    {
        shaderModels.remove(shaderModel);
    }

    /**
     * Add the given {@link ShaderModel} instances to this model
     * 
     * @param shaderModels The instances to add
     */
    public void addShaderModels(
        Collection<? extends DefaultShaderModel> shaderModels)
    {
        for (DefaultShaderModel shaderModel : shaderModels)
        {
            addShaderModel(shaderModel);
        }
    }

    /**
     * Return the {@link ShaderModel} at the given index
     *
     * @param index The index
     * @return The {@link ShaderModel}
     */
    public DefaultShaderModel getShaderModel(int index)
    {
        return shaderModels.get(index);
    }

    /**
     * Remove all {@link ShaderModel} instances
     */
    public void clearShaderModels()
    {
        shaderModels.clear();
    }
    
    /**
     * Returns an unmodifiable view on the list of {@link ShaderModel} 
     * instances that have been created for the glTF.
     * 
     * @return The {@link ShaderModel} instances
     */
    public List<ShaderModel> getShaderModels()
    {
        return Collections.unmodifiableList(shaderModels);
    }

    /**
     * Add the given {@link ProgramModel} to this model
     * 
     * @param programModel The instance to add
     */
    public void addProgramModel(DefaultProgramModel programModel)
    {
        programModels.add(programModel);
    }

    /**
     * Remove the given {@link ProgramModel} from this model
     * 
     * @param programModel The instance to remove
     */
    public void removeProgramModel(DefaultProgramModel programModel)
    {
        programModels.remove(programModel);
    }

    /**
     * Add the given {@link ProgramModel} instances to this model
     * 
     * @param programModels The instances to add
     */
    public void addProgramModels(
        Collection<? extends DefaultProgramModel> programModels)
    {
        for (DefaultProgramModel programModel : programModels)
        {
            addProgramModel(programModel);
        }
    }

    /**
     * Return the {@link ProgramModel} at the given index
     *
     * @param index The index
     * @return The {@link ProgramModel}
     */
    public DefaultProgramModel getProgramModel(int index)
    {
        return programModels.get(index);
    }

    /**
     * Remove all {@link ProgramModel} instances
     */
    public void clearProgramModels()
    {
        programModels.clear();
    }
    
    /**
     * Returns an unmodifiable view on the list of {@link ProgramModel} 
     * instances that have been created for the glTF.
     * 
     * @return The {@link ProgramModel} instances
     */
    public List<ProgramModel> getProgramModels()
    {
        return Collections.unmodifiableList(programModels);
    }
    
    
    /**
     * Add the given {@link TechniqueModel} to this model
     * 
     * @param techniqueModel The instance to add
     */
    public void addTechniqueModel(DefaultTechniqueModel techniqueModel)
    {
        techniqueModels.add(techniqueModel);
    }

    /**
     * Remove the given {@link TechniqueModel} from this model
     * 
     * @param techniqueModel The instance to remove
     */
    public void removeTechniqueModel(DefaultTechniqueModel techniqueModel)
    {
        techniqueModels.remove(techniqueModel);
    }

    /**
     * Add the given {@link TechniqueModel} instances to this model
     * 
     * @param techniqueModels The instances to add
     */
    public void addTechniqueModels(
        Collection<? extends DefaultTechniqueModel> techniqueModels)
    {
        for (DefaultTechniqueModel techniqueModel : techniqueModels)
        {
            addTechniqueModel(techniqueModel);
        }
    }

    /**
     * Return the {@link TechniqueModel} at the given index
     *
     * @param index The index
     * @return The {@link TechniqueModel}
     */
    public DefaultTechniqueModel getTechniqueModel(int index)
    {
        return techniqueModels.get(index);
    }

    /**
     * Remove all {@link TechniqueModel} instances
     */
    public void clearTechniqueModels()
    {
        techniqueModels.clear();
    }
    
    /**
     * Returns an unmodifiable view on the list of {@link TechniqueModel} 
     * instances that have been created for the glTF.
     * 
     * @return The {@link TechniqueModel} instances
     */
    public List<TechniqueModel> getTechniqueModels()
    {
        return Collections.unmodifiableList(techniqueModels);
    }
    
    /**
     * Return the element from the given getter, based on the 
     * {@link #indexMappingSet} for the given name and ID. 
     * If the ID is <code>null</code>, then <code>null</code> is 
     * returned. If there is no proper index stored for the given
     * ID, then a warning will be printed and <code>null</code>
     * will be returned.
     * 
     * @param <T> The element type
     * 
     * @param name The name
     * @param id The ID
     * @param getter The getter
     * @return The element
     */
    private <T> T get(String name, String id, IntFunction<? extends T> getter)
    {
        Integer index = indexMappingSet.getIndex(name, id);
        if (index == null)
        {
            logger.severe("No index found for " + name + " ID " + id);
            return null;
        }
        T element = getter.apply(index);
        return element;
    }
    
    
}
