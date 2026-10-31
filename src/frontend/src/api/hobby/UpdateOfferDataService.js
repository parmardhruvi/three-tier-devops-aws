import axios from "../customAxiosConfig/CustomAxiosConfig";

const UpdateOfferDataService = (hobby) => {
  return axios.put(`/hobbies`, hobby);
};

export default UpdateOfferDataService;
