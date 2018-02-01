package com.networknt.oauth.cache.model;
import java.io.IOException;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class ServiceEndpoint implements IdentifiedDataSerializable {

    
    private String operation;
    
    private String endpoint;
    
    private String scope;
    

    public ServiceEndpoint () {
    }

    
    
    @JsonProperty("operation")
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    
    
    @JsonProperty("endpoint")
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    
    
    @JsonProperty("scope")
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
    
    

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceEndpoint ServiceEndpoint = (ServiceEndpoint) o;

        return Objects.equals(operation, ServiceEndpoint.operation) &&
        Objects.equals(endpoint, ServiceEndpoint.endpoint) &&
        
        Objects.equals(scope, ServiceEndpoint.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, endpoint,  scope);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceEndpoint {\n");
        
        sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
        sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
        sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    @Override
    public int getFactoryId() {
        return ServiceEndpointDataSerializableFactory.ID;
    }

    @Override
    public int getId() {
        return ServiceEndpointDataSerializableFactory.SERVICE_ENDPOINT_TYPE;
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeUTF(this.endpoint);
        objectDataOutput.writeUTF(this.operation);
        objectDataOutput.writeUTF(this.scope);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        this.endpoint = objectDataInput.readUTF();
        this.operation = objectDataInput.readUTF();
        this.scope = objectDataInput.readUTF();
    }
}
