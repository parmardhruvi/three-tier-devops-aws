import React, { useState, useEffect } from "react";
import OfferInputField from "./fields/OfferInputField";
import OfferTextaria from "./fields/OfferTextaria";
import Footer from "../../../fragments/footer/Footer";
import { useNavigate, useLocation } from "react-router-dom";
import axios from "axios";
import AuthenticationService from "../../../../../api/authentication/AuthenticationService";
import UpdateOfferDataService from "../../../../../api/hobby/UpdateOfferDataService";
import HobbyDetailsDataService from "../../../../../api/hobby/HobbyDetailsDataService";
import styles from "../../../../../css/CreateOffer.module.css";
import style from "../../../../../css/Footer.module.css";
import LoadingDots from "./animation/LoadingDots";

const UpdateOffer = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  const username = AuthenticationService.getLoggedInUser();
  const [uploaded, setUploaded] = useState(false);

  const [files, setFiles] = useState({});

  const [info, setInfo] = useState({
    id: location.state?.id || "",
    name: location.state?.name || "",
    slogan: location.state?.slogan || "",
    intro: location.state?.intro || "",
    description: location.state?.description || "",
    price: location.state?.price || "",
    creator: username,
    contactInfo: location.state?.contactInfo || "",
    category:
      typeof location.state?.category === "object"
        ? location.state?.category?.name?.toUpperCase() || ""
        : (location.state?.category ? location.state.category.toUpperCase() : ""),
    location:
      typeof location.state?.location === "object"
        ? location.state?.location?.name?.toUpperCase() || ""
        : (location.state?.location ? location.state.location.toUpperCase() : ""),
    profileImgUrl: location.state?.profileImgUrl || "",
    galleryImgUrl1: location.state?.galleryImgUrl1 || "",
    galleryImgUrl2: location.state?.galleryImgUrl2 || "",
    galleryImgUrl3: location.state?.galleryImgUrl3 || "",
    profileImg_id: location.state?.profileImg_id || location.state?.profile_img_id || "",
    galleryImg1_id: location.state?.galleryImg1_id || location.state?.img1_id || "",
    galleryImg2_id: location.state?.galleryImg2_id || location.state?.img2_id || "",
    galleryImg3_id: location.state?.galleryImg3_id || location.state?.img3_id || "",
  });

  const [errors, setErrors] = useState({});

  // Direct backend fetch fallback if needed
  useEffect(() => {
    const hobbyId = location.state?.id;
    if (hobbyId && (!info.name || !info.category || !info.location)) {
      HobbyDetailsDataService(hobbyId).then((response) => {
        if (response && response.data) {
          const d = response.data;
          const cat = typeof d.category === "object" ? d.category?.name : d.category;
          const loc = typeof d.location === "object" ? d.location?.name : d.location;

          setInfo((prev) => ({
            ...prev,
            ...d,
            name: prev.name || d.name || "",
            slogan: prev.slogan || d.slogan || "",
            intro: prev.intro || d.intro || "",
            description: prev.description || d.description || "",
            price: prev.price || d.price || "",
            contactInfo: prev.contactInfo || d.contactInfo || "",
            category: prev.category || (cat || "").toString().toUpperCase(),
            location: prev.location || (loc || "").toString().toUpperCase(),
          }));
        }
      });
    }
  }, [location.state?.id]);

  const validate = () => {
    const errors = {};

    if (!files.profileImgUrl && !info.profileImgUrl) {
      errors.profileImgUrl = "Profile photo is required";
    }
    if (!info.name) {
      errors.name = "Hobby name is required";
    } else if (info.name.length < 3) {
      errors.name = "Hobby name must be at least 3 characters long";
    }

    if (!info.slogan) {
      errors.slogan = "Slogan is required";
    } else if (info.slogan.length < 2 || info.slogan.length > 20) {
      errors.slogan = "Text has to be between 2 and 20 characters long";
    }

    if (!info.category) {
      errors.category = "Category is required";
    }

    if (!info.intro) {
      errors.intro = "Intro is required";
    }

    if (!info.description) {
      errors.description = "Description is required";
    }

    if (!info.price) {
      errors.price = "Price is required";
    }

    if (!info.location) {
      errors.location = "Location is required";
    }

    if (!files.galleryImgUrl1 && !info.galleryImgUrl1) {
      errors.galleryImgUrl1 = "You must upload 3 high quality photos";
    }
    if (!files.galleryImgUrl2 && !info.galleryImgUrl2) {
      errors.galleryImgUrl2 = "You must upload 3 high quality photos";
    }
    if (!files.galleryImgUrl3 && !info.galleryImgUrl3) {
      errors.galleryImgUrl3 = "You must upload 3 high quality photos";
    }

    if (!info.contactInfo) {
      errors.contactInfo = "Contact info is required";
    }
    return errors;
  };

  const submitHandler = (event) => {
    event.preventDefault();

    let errors = validate();
    setErrors(errors);
    if (Object.keys(errors).length === 0) {
      setLoading(true);
      const uploadImage = async (file, oldUrl, oldId) => {
        if (!file) {
          return {
            url: oldUrl || "",
            id: oldId || "",
          };
        }

        const formData = new FormData();
        formData.append("file", file);
        formData.append("tags", "dv6ktrxwv, hobbie");
        formData.append("upload_preset", "vgf01lnc");
        formData.append("timestamp", (Date.now() / 1000) | 0);

        const { data } = await axios.post(
          "https://api.cloudinary.com/v1_1/dv6ktrxwv/image/upload",
          formData,
          {
            headers: { "X-Requested-With": "XMLHttpRequest" },
          }
        );

        return {
          url: data.secure_url,
          id: data.public_id,
        };
      };

      Promise.all([
        uploadImage(files.profileImgUrl, info.profileImgUrl, info.profileImg_id),
        uploadImage(files.galleryImgUrl1, info.galleryImgUrl1, info.galleryImg1_id),
        uploadImage(files.galleryImgUrl2, info.galleryImgUrl2, info.galleryImg2_id),
        uploadImage(files.galleryImgUrl3, info.galleryImgUrl3, info.galleryImg3_id),
      ])
        .then((images) => {
          const [profile, gallery1, gallery2, gallery3] = images;

          const updatedOffer = {
            ...info,
            id: Number(info.id),
            name: info.name,
            slogan: info.slogan,
            intro: info.intro,
            description: info.description,
            price: Number(info.price),
            creator: info.creator || username,
            contactInfo: info.contactInfo,
            category:
              typeof info.category === "object"
                ? info.category?.name?.toUpperCase()
                : (info.category || "").toUpperCase(),
            location:
              typeof info.location === "object"
                ? info.location?.name?.toUpperCase()
                : (info.location || "").toUpperCase(),
            profileImgUrl: profile.url,
            galleryImgUrl1: gallery1.url,
            galleryImgUrl2: gallery2.url,
            galleryImgUrl3: gallery3.url,
            profileImg_id: profile.id || info.profileImg_id || "",
            galleryImg1_id: gallery1.id || info.galleryImg1_id || "",
            galleryImg2_id: gallery2.id || info.galleryImg2_id || "",
            galleryImg3_id: gallery3.id || info.galleryImg3_id || "",
          };

          UpdateOfferDataService(updatedOffer)
            .then((res) => {
              setLoading(false);
              window.location.href = "/offer/" + info.id;
            })
            .catch((err) => {
              setLoading(false);
              console.error("Update failed:", err);
              const errMsg =
                err.response?.data?.message ||
                (typeof err.response?.data === "string" ? err.response?.data : "") ||
                err.message ||
                "Failed to update offer. Please try again.";
              alert(errMsg);
            });
        })
        .catch((error) => {
          console.error("Image upload failed:", error);
          setLoading(false);
          alert("Image upload failed. Please try again.");
        });
    } else {
      console.log(errors);
    }
  };

  return (
    <>
      <main className={styles.offer_main}>
        <h1 className={styles.title_offer}>Edit offer</h1>
        <form className={styles.form_offer} onSubmit={submitHandler}>
          <OfferInputField
            value={info.name || ""}
            defaultValue={info.name || ""}
            type="text"
            placeholder="ex: Painting, Swimming classes etc.."
            label="Hobbie Name"
            name="name"
            error={errors.name}
            onChange={(e) => setInfo({ ...info, name: e.target.value })}
          />
          <OfferInputField
            value={info.slogan || ""}
            defaultValue={info.slogan || ""}
            type="text"
            placeholder="your offer in one sentence..."
            label="Slogan"
            name="slogan"
            error={errors.slogan}
            onChange={(e) => setInfo({ ...info, slogan: e.target.value })}
          />

          <div className={styles.form_field_2}>
            <span>Category</span>
            <select
              value={info.category}
              onChange={(e) => setInfo({ ...info, category: e.target.value })}
              className={styles.custom_select}
              id="category"
              name="category"
            >
              <option value="">Select category</option>
              <option value="ACTIVE">Active</option>
              <option value="SOCIAL">Social</option>
              <option value="FUN">Fun</option>
              <option value="RELAX">Relax</option>
              <option value="INTELLECTUAL">Intellectual</option>
              <option value="CREATIVE">Creative</option>
              <option value="OTHER">Other</option>
            </select>
            {errors.category && (
              <small className={styles.errors_offer}>{errors.category}</small>
            )}
          </div>

          <OfferTextaria
            value={info.intro || ""}
            defaultValue={info.intro || ""}
            label="Intro"
            name="intro"
            error={errors.intro}
            onChange={(e) => setInfo({ ...info, intro: e.target.value })}
          />

          <OfferTextaria
            value={info.description || ""}
            defaultValue={info.description || ""}
            label="Description"
            name="description"
            error={errors.description}
            onChange={(e) => setInfo({ ...info, description: e.target.value })}
          />

          <OfferInputField
            value={info.price || ""}
            defaultValue={info.price || ""}
            type="number"
            placeholder=""
            label="Price per entry"
            name="price"
            error={errors.price}
            onChange={(e) => setInfo({ ...info, price: e.target.value })}
          />

          <div className={styles.form_field_2}>
            <span>Location</span>
            <select
              value={info.location}
              onChange={(e) => setInfo({ ...info, location: e.target.value })}
              className={styles.custom_select}
              id="location"
              name="location"
            >
              <option value="">Select location</option>
              <option value="ZURICH">Zurich</option>
              <option value="BERN">Bern</option>
              <option value="ZUG">Zug</option>
              <option value="LUZERN">Luzern</option>
            </select>
            {errors.location && (
              <small className={styles.errors_offer}>{errors.location}</small>
            )}
          </div>

          <div className={styles.form_field_photos}>
            <div className={styles.row_upload}>
              <label id="photo" className={styles.label_name}>
                <span>Gallery</span>
              </label>
              <article className={styles.button3}>
                <p className={styles.choose_file}>
                  {files.profileImgUrl || info.profileImgUrl ? "Photo uploaded" : "Choose a file"}
                </p>
                <input
                  onChange={(e) =>
                    setFiles({ ...files, profileImgUrl: e.target.files[0] })
                  }
                  type="file"
                  id="add-title-image"
                  name="img"
                />
              </article>
              <article className={styles.button3}>
                <p className={styles.choose_file}>
                  {files.galleryImgUrl1 || info.galleryImgUrl1 ? "Photo uploaded" : "Choose a file"}
                </p>
                <input
                  onChange={(e) =>
                    setFiles({ ...files, galleryImgUrl1: e.target.files[0] })
                  }
                  type="file"
                  id="add-title-image"
                  name="img"
                />
              </article>
              <article className={styles.button3}>
                <p className={styles.choose_file}>
                  {files.galleryImgUrl2 || info.galleryImgUrl2 ? "Photo uploaded" : "Choose a file"}
                </p>
                <input
                  onChange={(e) =>
                    setFiles({ ...files, galleryImgUrl2: e.target.files[0] })
                  }
                  type="file"
                  id="add-title-image"
                  name="img"
                />
              </article>
              <article className={styles.button3}>
                <p className={styles.choose_file}>
                  {files.galleryImgUrl3 || info.galleryImgUrl3 ? "Photo uploaded" : "Choose a file"}
                </p>
                <input
                  onChange={(e) =>
                    setFiles({ ...files, galleryImgUrl3: e.target.files[0] })
                  }
                  type="file"
                  id="add-title-image"
                  name="img"
                />
              </article>
              {(errors.galleryImgUrl1 ||
                errors.galleryImgUrl2 ||
                errors.galleryImgUrl3 ||
                errors.profileImgUrl) && (
                  <div className={styles.errors_offer}>
                    You must upload high quality photos
                  </div>
                )}
            </div>
          </div>

          <OfferTextaria
            value={info.contactInfo || ""}
            defaultValue={info.contactInfo || ""}
            label="Contact info"
            name="contact"
            error={errors.contactInfo}
            onChange={(e) =>
              setInfo({ ...info, contactInfo: e.target.value })
            }
          />

          {loading && <LoadingDots />}

          {!loading && (
            <button type="submit" className={styles.submit_offer}>
              Submit
            </button>
          )}
        </form>
      </main>

      <Footer class={style.footer_hobbie_details} />
    </>
  );
};

export default UpdateOffer;
