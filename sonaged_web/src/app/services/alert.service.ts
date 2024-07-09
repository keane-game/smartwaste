import Swal from "sweetalert2";

export function succesAlert(message: string){
    Swal.fire({
        position: "center",
        icon: "success",
        title: message,
        showConfirmButton: false,
        timer: 2600,
        customClass: {
          title: 'custom-swal-title-success'
        }
      });
}

export function errorAlert(message: string){
  Swal.fire({
      position: "center",
      icon: "error",
      title: message,
      showConfirmButton: false,
      timer: 3000,
      customClass: {
        title: 'custom-swal-title-error'
      }
    });
}