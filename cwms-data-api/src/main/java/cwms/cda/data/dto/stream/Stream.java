package cwms.cda.data.dto.stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import cwms.cda.api.errors.FieldException;
import cwms.cda.data.dto.CwmsDTOBase;
import cwms.cda.data.dto.LocationIdentifier;
import cwms.cda.formatters.Formats;
import cwms.cda.formatters.annotations.FormattableWith;
import cwms.cda.formatters.json.JsonV1;

@FormattableWith(contentType = Formats.JSONV1, formatter = JsonV1.class, aliases = {Formats.DEFAULT, Formats.JSON})
@JsonDeserialize(builder = Stream.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
public final class Stream implements CwmsDTOBase {

    private final Boolean startsDownstream;
    private final StreamNode flowsIntoStreamNode;
    private final StreamNode divertsFromStreamNode;
    private final Double length;
    private final Double slope;
    private final String lengthUnit;
    private final String slopeUnit;
    private final String comment;
    private final LocationIdentifier id;

    private Stream(Builder builder) {
        this.startsDownstream = builder.startsDownstream;
        this.flowsIntoStreamNode = builder.flowsIntoStreamNode;
        this.divertsFromStreamNode = builder.divertsFromStreamNode;
        this.length = builder.length;
        this.slope = builder.slope;
        this.lengthUnit = builder.lengthUnit;
        this.slopeUnit = builder.slopeUnit;
        this.comment = builder.comment;
        this.id = builder.id;
    }

    @Override
    public void validate() throws FieldException {
        if (this.id == null) {
            throw new FieldException("The 'id' field of a Stream cannot be null.");
        }
        id.validate();
    }

    public Boolean getStartsDownstream() {
        return startsDownstream;
    }

    @JsonIgnore
    public String getOfficeId() {
        return id.getOfficeId();
    }

    public StreamNode getFlowsIntoStreamNode() {
        return flowsIntoStreamNode;
    }

    public StreamNode getDivertsFromStreamNode() {
        return divertsFromStreamNode;
    }

    public Double getLength() {
        return length;
    }

    public Double getSlope() {
        return slope;
    }

    public String getLengthUnit() {
        return lengthUnit;
    }

    public String getSlopeUnit() {
        return slopeUnit;
    }

    public String getComment() {
        return comment;
    }

    public LocationIdentifier getId() {
        return id;
    }

    public static final class Builder {
        private Boolean startsDownstream;
        private StreamNode flowsIntoStreamNode;
        private StreamNode divertsFromStreamNode;
        private Double length;
        private Double slope;
        private String lengthUnit;
        private String slopeUnit;
        private String comment;
        private LocationIdentifier id;

        public Builder withStartsDownstream(Boolean startsDownstream) {
            this.startsDownstream = startsDownstream;
            return this;
        }

        public Builder withFlowsIntoStreamNode(StreamNode flowsIntoStreamNode) {
            this.flowsIntoStreamNode = flowsIntoStreamNode;
            return this;
        }

        public Builder withDivertsFromStreamNode(StreamNode divertsFromStreamNode) {
            this.divertsFromStreamNode = divertsFromStreamNode;
            return this;
        }

        public Builder withLength(Double length) {
            this.length = length;
            return this;
        }

        public Builder withSlope(Double slope) {
            this.slope = slope;
            return this;
        }

        public Builder withLengthUnit(String lengthUnit) {
            this.lengthUnit = lengthUnit;
            return this;
        }

        public Builder withSlopeUnit(String slopeUnit) {
            this.slopeUnit = slopeUnit;
            return this;
        }

        public Builder withComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder withId(LocationIdentifier id) {
            this.id = id;
            return this;
        }

        public Stream build() {
            return new Stream(this);
        }
    }
}