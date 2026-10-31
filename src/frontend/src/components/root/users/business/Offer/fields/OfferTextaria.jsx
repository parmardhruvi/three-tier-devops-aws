import React from "react";
import styles from "../../../../../../css/CreateOffer.module.css";

const OfferTextaria = (props) => {
  return (
    <div className={styles.form_field_2}>
      <section className={styles.name_section}>
        <label htmlFor={props.name} className={styles.label_name}>
          <span>{props.label}</span>
        </label>
        <textarea
          value={props.value !== undefined ? props.value : props.defaultValue}
          defaultValue={props.value !== undefined ? undefined : props.defaultValue}
          onChange={props.onChange}
          className={styles.label_name}
          type="text"
          id={props.name}
          name={props.name}
        ></textarea>
      </section>
      {props.error && (
        <small className={styles.errors_offer}>{props.error}</small>
      )}
    </div>
  );
};

export default OfferTextaria;
