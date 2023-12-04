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

        ///////////////////////////////////////////////////////////////////////
        // EXTMETA
        ///////////////////////////////////////////////////////////////////////

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

        ///////////////////////////////////////////////////////////////////////
        // ENCODINGDESC
        ///////////////////////////////////////////////////////////////////////

        public EncodingDesc getEncodingDesc() {
            return encodingDesc;
        }

        public void setEncodingDesc(EncodingDesc encodingDesc) {
            this.encodingDesc = encodingDesc;
        }

        @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
        public EncodingDesc encodingDesc;

        @Serdeable
        @Introspected
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class EncodingDesc {

            public AppInfo getAppInfo() {
                return appInfo;
            }

            public void setAppInfo(AppInfo appInfo) {
                this.appInfo = appInfo;
            }

            @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
            public AppInfo appInfo;

            @Serdeable
            @Introspected
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class AppInfo {
                public List<Application> getApplication() {
                    return application;
                }

                public void setApplication(List<Application> application) {
                    this.application = application;
                }

                @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                public List<Application> application;

                @Serdeable
                @Introspected
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class Application {
                    public String getIsodate() {
                        return isodate;
                    }

                    public void setIsodate(String isodate) {
                        this.isodate = isodate;
                    }

                    @XmlAttribute
                    public String isodate;

                    public String getVersion() {
                        return version;
                    }

                    public void setVersion(String version) {
                        this.version = version;
                    }

                    @XmlAttribute
                    public String version;

                    public ApplicationName getName() {
                        return name;
                    }

                    public void setName(ApplicationName name) {
                        this.name = name;
                    }

                    @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                    public ApplicationName name;

                    @Serdeable
                    @Introspected
                    @XmlAccessorType(XmlAccessType.FIELD)
                    public static class ApplicationName {
                        public String getType() {
                            return type;
                        }

                        public void setType(String type) {
                            this.type = type;
                        }

                        @XmlAttribute
                        public String type;

                        public String getValue() {
                            return value;
                        }

                        public void setValue(String value) {
                            this.value = value;
                        }

                        @XmlValue
                        public String value;
                    }

                    public String getP() {
                        return p;
                    }

                    public void setP(String p) {
                        this.p = p;
                    }

                    @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                    public String p;

                    public String getType() {
                        return type;
                    }

                    public void setType(String type) {
                        this.type = type;
                    }

                    @XmlAttribute
                    public String type;
                }
            }

            public ProjectDesc getProjectDesc() {
                return projectDesc;
            }

            public void setProjectDesc(ProjectDesc projectDesc) {
                this.projectDesc = projectDesc;
            }

            @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
            public ProjectDesc projectDesc;

            @Serdeable
            @Introspected
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class ProjectDesc {

                public List<P> getP() {
                    return p;
                }

                public void setP(List<P> p) {
                    this.p = p;
                }

                @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                public List<P> p;

                @Serdeable
                @Introspected
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class P {
                    public String getValue() {
                        return value;
                    }

                    public void setValue(String value) {
                        this.value = value;
                    }

                    @XmlValue
                    public String value;
                }
            }

        }

        ///////////////////////////////////////////////////////////////////////
        // FILEDESC
        ///////////////////////////////////////////////////////////////////////

        public FileDesc getFileDesc() {
            return this.fileDesc;
        }

        public void setFileDesc(FileDesc fileDesc) {
            this.fileDesc = fileDesc;
        }

        @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
        public FileDesc fileDesc;

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
                public List<Title> getTitle() {
                    return title;
                }

                public void setTitle(List<Title> title) {
                    this.title = title;
                }

                @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                public List<Title> title;

                @Serdeable
                @Introspected
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class Title {
                    public String getAnalog() {
                        return analog;
                    }

                    public void setAnalog(String analog) {
                        this.analog = analog;
                    }

                    @XmlAttribute
                    public String analog;

                    public String getType() {
                        return type;
                    }

                    public void setType(String type) {
                        this.type = type;
                    }

                    @XmlAttribute
                    public String type;

                    public String getValue() {
                        return value;
                    }

                    public void setValue(String value) {
                        this.value = value;
                    }

                    @XmlValue
                    public String value;
                }

                public RespStmt getRespStmt() {
                    return respStmt;
                }

                public void setRespStmt(RespStmt respStmt) {
                    this.respStmt = respStmt;
                }

                @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                public RespStmt respStmt;

                @Serdeable
                @Introspected
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class RespStmt {
                    public List<ResponsibilityStatementName> getName() {
                        return name;
                    }

                    public void setName(List<ResponsibilityStatementName> name) {
                        this.name = name;
                    }

                    @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                    public List<ResponsibilityStatementName> name;

                    @Serdeable
                    @Introspected
                    @XmlAccessorType(XmlAccessType.FIELD)
                    public static class ResponsibilityStatementName {
                        public String getRole() {
                            return this.role;
                        }

                        public void setRole(String role) {
                            this.role = role;
                        }

                        @XmlAttribute
                        public String role;

                        public String getValue() {
                            return value;
                        }

                        public void setValue(String value) {
                            this.value = value;
                        }

                        @XmlValue
                        public String value;
                    }

                    public List<PersName> getPersName() {
                        return persName;
                    }

                    public void setPersName(List<PersName> persName) {
                        this.persName = persName;
                    }

                    @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                    public List<PersName> persName;

                    @Serdeable
                    @Introspected
                    @XmlAccessorType(XmlAccessType.FIELD)
                    public static class PersName {
                        public String getRole() {
                            return this.role;
                        }

                        public void setRole(String role) {
                            this.role = role;
                        }

                        @XmlAttribute
                        public String role;

                        public String getValue() {
                            return value;
                        }

                        public void setValue(String value) {
                            this.value = value;
                        }

                        @XmlValue
                        public String value;


                        public String getAnalog() {
                            return analog;
                        }

                        public void setAnalog(String analog) {
                            this.analog = analog;
                        }

                        @XmlValue
                        public String analog;
                    }
                }

                public String getComposer() {
                    return composer;
                }

                public void setComposer(String composer) {
                    this.composer = composer;
                }

                @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                String composer;
            }

            public PubStmt getPubStmt() {
                return pubStmt;
            }

            public void setPubStmt(PubStmt pubStmt) {
                this.pubStmt = pubStmt;
            }

            @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
            public PubStmt pubStmt;

            @Serdeable
            @Introspected
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class PubStmt {
                public TitleStmt.RespStmt getRespStmt() {
                    return respStmt;
                }

                public void setRespStmt(TitleStmt.RespStmt respStmt) {
                    this.respStmt = respStmt;
                }

                @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                public TitleStmt.RespStmt respStmt;

                public Date getDate() {
                    return date;
                }

                public void setDate(Date date) {
                    this.date = date;
                }

                @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                public Date date;

                @Serdeable
                @Introspected
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class Date {
                    public String getValue() {
                        return value;
                    }

                    public void setValue(String value) {
                        this.value = value;
                    }

                    @XmlValue
                    public String value;

                    public String getIsodate() {
                        return isodate;
                    }

                    public void setIsodate(String isodate) {
                        this.isodate = isodate;
                    }

                    @XmlAttribute
                    public String isodate;

                    public String getType() {
                        return type;
                    }

                    public void setType(String type) {
                        this.type = type;
                    }

                    @XmlAttribute
                    public String type;
                }

                public Availability getAvailability() {
                    return availability;
                }

                public void setAvailability(Availability availability) {
                    this.availability = availability;
                }

                @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                public Availability availability;

                @Serdeable
                @Introspected
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class Availability {
                    @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                    public String distributor;

                    public String getDistributor() {
                        return distributor;
                    }

                    public void setDistributor(String distributor) {
                        this.distributor = distributor;
                    }
                }
            }

            public SourceDesc getSourceDesc() {
                return sourceDesc;
            }

            public void setSourceDesc(SourceDesc sourceDesc) {
                this.sourceDesc = sourceDesc;
            }

            @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
            public SourceDesc sourceDesc;

            @Serdeable
            @Introspected
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class SourceDesc {

                public Source getSource() {
                    return source;
                }

                public void setSource(Source source) {
                    this.source = source;
                }

                @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                public Source source;

                @Serdeable
                @Introspected
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class Source {
                    public TitleStmt getTitleStmt() {
                        return titleStmt;
                    }

                    public void setTitleStmt(TitleStmt titleStmt) {
                        this.titleStmt = titleStmt;
                    }

                    @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                    public TitleStmt titleStmt;


                    public PubStmt getPubStmt() {
                        return pubStmt;
                    }

                    public void setPubStmt(PubStmt pubStmt) {
                        this.pubStmt = pubStmt;
                    }

                    @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
                    public PubStmt pubStmt;
                }
            }
        }

        ///////////////////////////////////////////////////////////////////////
        // WORKDESC
        ///////////////////////////////////////////////////////////////////////

        public WorkDesc getWorkDesc() {
            return workDesc;
        }

        public void setWorkDesc(WorkDesc workDesc) {
            this.workDesc = workDesc;
        }

        @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
        public WorkDesc workDesc;

        @Serdeable
        @Introspected
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class WorkDesc {
            public Work getWork() {
                return work;
            }

            public void setWork(Work work) {
                this.work = work;
            }

            @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
            public Work work;
        }

        ////////////////////////////////////////////////////////////////////////
        // WORKLIST
        ///////////////////////////////////////////////////////////////////////

        public List<Work> getWorkList() {
            return this.workList;
        }

        public void setWorkList(List<Work> workList) {
            this.workList = workList;
        }

        @XmlElementWrapper(name = "workList", namespace = "http://www.music-encoding.org/ns/mei")
        @XmlElement(name = "work", namespace = "http://www.music-encoding.org/ns/mei")
        public List<Work> workList;

        @Serdeable
        @Introspected
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Work {
            public List<FileDesc.TitleStmt.Title> getTitle() {
                return title;
            }

            public void setTitle(List<FileDesc.TitleStmt.Title> title) {
                this.title = title;
            }

            @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
            public List<FileDesc.TitleStmt.Title> title;

            public String getComposer() {
                return composer;
            }

            public void setComposer(String composer) {
                this.composer = composer;
            }

            @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
            public String composer;

            public FileDesc.TitleStmt getTitleStmt() {
                return titleStmt;
            }

            public void setTitleStmt(FileDesc.TitleStmt titleStmt) {
                this.titleStmt = titleStmt;
            }

            @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
            FileDesc.TitleStmt titleStmt;

            public Identifier getIdentifier() {
                return identifier;
            }

            public void setIdentifier(Identifier identifier) {
                this.identifier = identifier;
            }

            @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
            public Identifier identifier;

            @Serdeable
            @Introspected
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class Identifier {
                public String getAnalog() {
                    return analog;
                }

                public void setAnalog(String analog) {
                    this.analog = analog;
                }

                @XmlAttribute
                public String analog;

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }

                @XmlValue
                public String value;
            }
        }
    }
}
