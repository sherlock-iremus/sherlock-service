package fr.cnrs.iremus.sherlock.xml.mei;


import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.xml.bind.annotation.*;

import java.util.List;

@Serdeable
@Introspected
@XmlRootElement(name = "mei")
@XmlAccessorType(XmlAccessType.FIELD)
public class Mei {
    public MeiHead getMeiHead() {
        return meiHead;
    }

    public void setMeiHead(MeiHead meiHead) {
        this.meiHead = meiHead;
    }

    @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
    public MeiHead meiHead;

    @Serdeable
    @Introspected
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MeiHead {
        public ExtMeta getExtMeta() {
            return extMeta;
        }

        public void setExtMeta(ExtMeta extMeta) {
            this.extMeta = extMeta;
        }

        @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
        public ExtMeta extMeta;

        @Serdeable
        @Introspected
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class FileDesc {
            public TitleStmt getTitleStmt() {
                return titleStmt;
            }

            public void setTitleStmt(TitleStmt titleStmt) {
                this.titleStmt = titleStmt;
            }

            @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
            public TitleStmt titleStmt;

            @Serdeable
            @Introspected
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class TitleStmt {
                public String getTitle() {
                    return title;
                }

                public void setTitle(String title) {
                    this.title = title;
                }

                @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                public String title;
            }
        }

        @Serdeable
        @Introspected
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class ExtMeta {
            public List<MetaFrame> getFrames() {
                return frames;
            }

            public void setFrames(List<MetaFrame> frames) {
                this.frames = frames;
            }

            @XmlElementWrapper(name = "frames", namespace = "http://www.humdrum.org/ns/humxml")
            @XmlElement(name = "metaFrame", namespace = "http://www.humdrum.org/ns/humxml")
            public List<MetaFrame> frames;

            @Serdeable
            @Introspected
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class MetaFrame {
                public FrameInfo getFrameInfo() {
                    return frameInfo;
                }

                public void setFrameInfo(FrameInfo frameInfo) {
                    this.frameInfo = frameInfo;
                }

                @XmlElement(namespace = "http://www.humdrum.org/ns/humxml")
                public FrameInfo frameInfo;

                @Serdeable
                @Introspected
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class FrameInfo {
                    public String getReferenceKey() {
                        return referenceKey;
                    }

                    public void setReferenceKey(String referenceKey) {
                        this.referenceKey = referenceKey;
                    }

                    @XmlElement(namespace = "http://www.humdrum.org/ns/humxml")
                    public String referenceKey;

                    public String getReferenceValue() {
                        return referenceValue;
                    }

                    public void setReferenceValue(String referenceValue) {
                        this.referenceValue = referenceValue;
                    }

                    @XmlElement(namespace = "http://www.humdrum.org/ns/humxml")
                    public String referenceValue;
                }
            }
        }
    }
}